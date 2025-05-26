# Simple test cases for the calculator

def test_addition():
    assert 5 + 3 == 8

def test_subtraction():
    assert 10 - 4 == 6

def test_multiplication():
    assert 6 * 2 == 12

def test_division():
    assert 8 / 2 == 4.0

def test_division_by_zero():
    try:
        result = 10 / 0
        assert False  # This line should never be reached
    except ZeroDivisionError:
        assert True  # Expected behavior