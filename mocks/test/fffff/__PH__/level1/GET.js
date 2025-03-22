function evaluate() {
    var sum = 10;
    for (var i = 0; i < 10; i++) {
        sum += sum;
    }
    console.log("hello");
    return "hello world - " + sum;
}

evaluate();