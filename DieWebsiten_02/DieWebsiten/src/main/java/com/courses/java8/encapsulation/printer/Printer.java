package com.courses.java8.encapsulation.printer;

public class Printer {
	
	private int toner;
	private int pages;
	
	public Printer() {
		this.toner = 100;
		this.pages = 0;
	}
	
	public void print(boolean duplex) {
		System.out.println("printing...");
		
		try {
			if (duplex && getToner() < 10) {
				throw new Exception("Toner does not have enough ink to print this duplex page.");
			} else if (!duplex && getToner() < 5) {
				throw new Exception("Toner does not have enough ink to print this simple page.");
			}
		} catch (Exception e) {
			System.out.println("============ WARNING ============");
			System.out.println(e.getMessage());
			fillUpToner();
		}
			
		setToner(duplex);
		setPages();	
		System.out.println("done!");
	}
	
	private void fillUpToner () {
		System.out.println("Filling up toner...");
		this.toner = 100;
		System.out.println("Toner is now up to 100%.");
	}

	private int getToner() {
		return this.toner;
	}

	public int getPages() {
		return this.pages;
	}

	private void setToner(boolean duplex) {
		if (duplex) {
			this.toner -= 10;
		} else {
			this.toner -= 5;			
		}
	}

	private void setPages() {
		this.pages++;
	}

}
