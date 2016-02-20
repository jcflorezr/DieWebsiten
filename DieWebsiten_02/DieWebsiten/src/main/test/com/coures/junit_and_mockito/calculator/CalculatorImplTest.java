package com.coures.junit_and_mockito.calculator;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.courses.junit_and_mockito.calculator.Calculator;
import com.courses.junit_and_mockito.calculator.CalculatorImpl;

@RunWith(Parameterized.class)
public class CalculatorImplTest {

	private int num1;
	private int num2;
	private int expectedResult;
	
	public CalculatorImplTest(int num1, int num2, int expectedResult) {
		this.num1 = num1;
		this.num2 = num2;
		this.expectedResult = expectedResult;
	}
	
	@Parameters
	public static Collection<Integer[]> parameters () {
		return Arrays.asList(new Integer[][]{{-1, -2, -3}, {1, 2, 3}, {6, 7, 13}});
	}
	
	@Test
	public void test() {
		Calculator c = new CalculatorImpl();
		int result = c.add(num1, num2);
		assertEquals(expectedResult, result);
	}

}
