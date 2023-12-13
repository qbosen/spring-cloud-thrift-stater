namespace java top.abosen.thrift.demo.calculator.thrift

exception CalculateException {
    1: i32 code,
    2: string message
}

service CalculatorService {
    i32 add(1: i32 arg1, 2: i32 arg2)
    i32 subtract(1: i32 arg1, 2: i32 arg2)
    i32 multiply(1: i32 arg1, 2: i32 arg2)
    i32 division(1: i32 arg1, 2: i32 arg2) throws (1: CalculateException e)
}