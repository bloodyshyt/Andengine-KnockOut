package com.cslabs.knockout.entity;

public enum PlayerNo {
	P1(1), P2(2), P3(3), P4(4);
	
	private int value;

    private PlayerNo(int value) {
            this.setValue(value);
    }

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
