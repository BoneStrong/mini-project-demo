package com.dzz.graphql.interf;


import java.util.function.Function;

public interface RestControllerResponseProvider<R> {

    <T> Function<T, R> responseOkFunction();

    <T, C, E extends Throwable> Function3X<T, C, E, R> responseExceptionFunction();

    interface Function3X<T1, T2, T3, R> {
        R apply(T1 t1, T2 t2, T3 t3);
    }

    class DefaultRestControllerResponseProvider implements RestControllerResponseProvider<Response> {

        @Override
        public <T> Function<T, Response> responseOkFunction() {
            return Response::success;
        }

        @Override
        public <T, C, E extends Throwable> Function3X<T, C, E, Response> responseExceptionFunction() {
            return (T data, C code, E exception) -> {
                if (exception != null) return Response.fail(code.toString(), exception.getMessage());
                return Response.fail(code.toString(), data.toString());
            };
        }
    }

}
