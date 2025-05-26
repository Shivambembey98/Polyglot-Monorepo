# Simple CLI Calculator

def calculator():
    print("Simple CLI Calculator")
    print("Choose an operation: +, -, *, /")
    
    op = input("Enter operation: ")
    if op not in ["+", "-", "*", "/"]:
        print("Invalid operation")
        return

    num1 = input("Enter first number: ")
    num2 = input("Enter second number: ")

    if not num1.isdigit() or not num2.isdigit():
        print("Invalid input. Please enter numbers only.")
        return

    num1, num2 = int(num1), int(num2)

    if op == "+":
        print("Result:", num1 + num2)
    elif op == "-":
        print("Result:", num1 - num2)
    elif op == "*":
        print("Result:", num1 * num2)
    elif op == "/":
        if num2 == 0:
            print("Error: Division by zero is not allowed.")
        else:
            print("Result:", num1 / num2)

if __name__ == "__main__":
    calculator()
