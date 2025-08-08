package me.hhh.amonplugin.Helpers;

import java.util.Stack;

public class ShuntingYard {

    public static double evaluateExpression(String expression) {

        Stack<Double> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                StringBuilder number = new StringBuilder();

                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i++));
                }
                i--;
                operandStack.push(Double.parseDouble(number.toString()));
            }

            else if (c == '(') {
                operatorStack.push(c);
            }
            else if (c == ')') {
                while (operatorStack.peek() != '(') {
                    operandStack.push(applyOperator(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                }
                operatorStack.pop();
            }
            else if (isOperator(c)) {

                while (!operatorStack.isEmpty() && precedence(c) <= precedence(operatorStack.peek())) {
                    operandStack.push(applyOperator(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                }

                operatorStack.push(c);
            }
        }


        while (!operatorStack.isEmpty()) {
            operandStack.push(applyOperator(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
        }


        return operandStack.pop();
    }


    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }


    private static int precedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
        }
        return -1;
    }

    private static double applyOperator(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                return a / b;
        }
        return 0;
    }
}
