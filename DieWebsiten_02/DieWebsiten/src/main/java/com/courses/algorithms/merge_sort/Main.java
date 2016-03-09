package com.courses.algorithms.merge_sort;

public class Main {

	public static void main(String[] args) {
		
		int[] A = new int[]{7,3,5,1,6,4,2,9,8};
		int[] B = new int[A.length/2];
		int[] C = new int[A.length - A.length/2];
		
		for (int i=0; i<B.length; i++) {
			B[i] = A[i];
		}
		
		for (int i=B.length; i<A.length; i++) {
			C[i-B.length] = A[i];
		} 
		
		int[] K = new int[A.length];

	}

}
