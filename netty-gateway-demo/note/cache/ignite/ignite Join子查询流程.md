- select 请求经过ignite server Nio解析分发至 GridQueryProcessor 处理
- GridQueryProcessor.executeQuery()实际交由 GridQueryIndexing 代理至 IgniteH2Indexing

### IgniteH2Index
- 保存整张表b+树 数据

- executeSelect0() -->  GridCacheTwoStepQuery twoStepQry = select.forUpdate() && inTx ?
  select.forUpdateTwoStepQuery() : select.twoStepQuery(); 两段式select
  
- twoStepQry.executeSelectDistributed 本质是 IgniteH2Indexing.this.rdcQryExec.query
- ignite将查询的实现包装成 iterator ,服务端在响应客户端时才调用。可以在ClientCacheQueryCursor
    的writePage查看，反向调用GridReduceQueryExecutor.query()
  
- iterator将请求包装成 GridH2QueryRequest 

- GridMapQueryExecutor.onQueryRequest(node, (GridH2QueryRequest)msg) 处理H2Query请求
- 转发给 GridCacheSqlQuery qry ，**本质还是给H2解析sql,前面绕这么一大圈子简直是有病**


### H2 SQL解析子查询
1. SQL语句会被h2解析成Prepared对象，然后再调用它的update()方法，其中：
    - DDL会编译成DefineCommand
    - DML会编译成Query, Update, Delete等对象
    
2. Query有2个派生类：Select和SelectUnion

####  Select.query()
Query有个核心字段topTableFilter,内部有Condition，即sql 的where查询条件

h2版本是1.4.197
query查询默认queryWithoutCache(),实际是queryFlat()
query 查询结果由LocalResult保存，localResult.next()调用的是topTableFilter.next()

- tableFilter.next() 
    - IndexCursor.find(session, indexConditions)
        - IndexCursor.prepare(s, indexConditions) //重要步骤，解析子查询，
```text
 public void prepare(Session s, ArrayList<IndexCondition> indexConditions) {
        this.session = s;alwaysFalse = false;
        start = end = null;
        inList = null;
        inColumn = null;
        inResult = null;
        inResultTested = null;
        intersects = null;
        // don't use enhanced for loop to avoid creating objects
        for (IndexCondition condition : indexConditions) {
            if (condition.isAlwaysFalse()) {
                alwaysFalse = true;
                break;
            }
            // If index can perform only full table scan do not try to use it for regular
            // lookups, each such lookup will perform an own table scan.
            if (index.isFindUsingFullTableScan()) {
                continue;
            }
            Column column = condition.getColumn();
            if (condition.getCompareType() == Comparison.IN_LIST) {
                if (start == null && end == null) {
                    if (canUseIndexForIn(column)) {
                        this.inColumn = column;
                        inList = condition.getCurrentValueList(s);
                        inListIndex = 0;
                    }
                }
            } else if (condition.getCompareType() == Comparison.IN_QUERY) {
                if (start == null && end == null) {
                    if (canUseIndexForIn(column)) {
                        this.inColumn = column;
                        inResult = condition.getCurrentResult();
                    }
                }
            } else {
                Value v = condition.getCurrentValue(s);
                boolean isStart = condition.isStart();
                boolean isEnd = condition.isEnd();
                boolean isIntersects = condition.isSpatialIntersects();
                int columnId = column.getColumnId();
                if (columnId >= 0) {
                    IndexColumn idxCol = indexColumns[columnId];
                    if (idxCol != null && (idxCol.sortType & SortOrder.DESCENDING) != 0) {
                        // if the index column is sorted the other way, we swap
                        // end and start NULLS_FIRST / NULLS_LAST is not a
                        // problem, as nulls never match anyway
                        boolean temp = isStart;
                        isStart = isEnd;
                        isEnd = temp;
                    }
                }
                if (isStart) {
                    start = getSearchRow(start, columnId, v, true);
                }
                if (isEnd) {
                    end = getSearchRow(end, columnId, v, false);
                }
                if (isIntersects) {
                    intersects = getSpatialSearchRow(intersects, columnId, v);
                }
                // An X=? condition will produce less rows than
                // an X IN(..) condition, unless the X IN condition can use the index.
                if ((isStart || isEnd) && !canUseIndexFor(inColumn)) {
                    inColumn = null;
                    inList = null;
                    inResult = null;
                }
                if (!session.getDatabase().getSettings().optimizeIsNull) {
                    if (isStart && isEnd) {
                        if (v == ValueNull.INSTANCE) {
                            // join on a column=NULL is always false
                            alwaysFalse = true;
                        }
                    }
                }
            }
        }
        if (inColumn != null) {
            start = table.getTemplateRow();
        }
    }
```
IndexCursor 解析indexCondition


```text
protected Value[] fetchNextRow() {
            while ((sampleSize <= 0 || rowNumber < sampleSize) &&
                    topTableFilter.next()) {
                setCurrentRowNumber(rowNumber + 1);
                if (isConditionMet()) {
                    ++rowNumber;
                    Value[] row = new Value[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        Expression expr = expressions.get(i);
                        row[i] = expr.getValue(getSession());
                    }
                    return row;
                }
            }
            return null;
        }
```
例子：




