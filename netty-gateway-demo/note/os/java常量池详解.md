java常量池

java进行类加载的时候需要对class文件进行加载，链接和初始化的过程。
class文件中定义的常量池在JVM加载之后会发生什么神奇的变化呢？

首先每个class文件都有一个常量池（class文件常量池），包括以下东西：
1. 字符串常量
2. 类和接口名字
3. 字段名
4. 其他class中的引用常量

但是只有class文件常量池是不够用的，jvm运行的过程中引用是可能发生变化的。

这个时候就多出一个常量池-运行时常量池，为jvm运行服务。

**运行时常量池和class文件的常量池是一一对应的**，它就是class文件的常量池来构建的。

运行时常量池中有两种类型：
- symbolic references符号引用 
- static constants静态常量。

其中静态常量不需要后续解析，而符号引用需要进一步进行解析处理。

什么是静态常量，什么是符号引用呢？ 我们举个直观的例子。

```text
String site="hehe"
```

上面的字符串`hehe`可以看做是一个静态常量，因为它是不会变化的，是什么样的就展示什么样的。
> 运行时常量池中的静态常量是从class文件中的constant_pool构建的
> 可以分为两部分：String常量和数字常量。
> **String常量是对String对象的引用**，是从class中的CONSTANT_String_info结构体构建的。

#### String常量
```text
CONSTANT_String_info {
u1 tag;  //tag就是结构体的标记，string_index是string在class常量池的index。
u2 string_index;  //string_index对应的class常量池的内容是一个CONSTANT_Utf8_info结构体。
}

CONSTANT_Utf8_info {
    u1 tag;
    u2 length;
    u1 bytes[length];
}
//CONSTANT_Utf8_info是啥呢？它就是要创建的String对象的变种UTF-8编码。
```
讲完class文件中CONSTANT_String_info的结构之后，我们再来看看从CONSTANT_String_info创建运行时String常量的规则：

- 规则一：如果String.intern之前被调用过，并且返回的结果和CONSTANT_String_info中保存的编码是一致的话，
  表示他们指向的是同一个String的实例。

- 规则二：如果不同的话，那么会创建一个新的String实例，并将运行时String常量指向该String的实例。
  最后会在这个String实例上调用String的intern方法。调用intern方法主要是将这个String实例加入字符串常量池。




CONSTANT_Utf8_info {
u1 tag;
u2 length;
u1 bytes[length];
}

**String常量是对String对象的引用，那么这些创建的String对象是放在什么地方呢**？

没错，就是String Pool字符串常量池。

这个String Pool在每个JVM中都只会维护一份。是所有的类共享的。

String Pool是在1.6之前是存放在方法区的。在1.8之后被放到了java heap中。



而上面的字符串的名字“site”就是符号引用，需要在运行期间进行解析，
因为site的值是可以变化的，我们不能在第一时间确定其真正的值，需要在动态运行中进行解析。
>符号引用也是从class中的constant_pool中构建的。

1. 对class和interface的符号引用来自于CONSTANT_Class_info。
2. 对class和interface中字段的引用来自于CONSTANT_Fieldref_info。
3. class中方法的引用来自于CONSTANT_Methodref_info。
4. interface中方法的引用来自于CONSTANT_InterfaceMethodref_info。
5. 对方法句柄的引用来自于CONSTANT_MethodHandle_info。
6. 对方法类型的引用来自于CONSTANT_MethodType_info。
7. 对动态计算常量的符号引用来自于CONSTANT_MethodType_info。
8. 对动态计算的call site的引用来自于CONSTANT_InvokeDynamic_info。


总结
class文件中常量池保存的是字符串常量，类和接口名字，字段名，和其他一些在class中引用的常量。每个class都有一份。

运行时常量池保存的是从class文件常量池构建的静态常量引用和符号引用。每个class都有一份。

字符串常量池保存的是“字符”的实例，供运行时常量池引用。

运行时常量池是和class或者interface一一对应的，那么如果一个class生成了两个实例对象，
这两个实例对象是共享一个运行时常量池还是分别生成两个不同的常量池呢？
肯定是一个，运行时常量池保存的是字符实例，对象实例里面是带有字符实例的引用，
如果有两个， 想想也觉得设计傻逼


#### 思考
#####  String a =new String(“abc”)创建了几个对象？
String a =new String(“abc”) 实际上是创建了两个对象（假设之前String的常量池中没有创建任何对象），
一个是“abc” （在字符串常量池中），一个是new String()（在堆中）。
