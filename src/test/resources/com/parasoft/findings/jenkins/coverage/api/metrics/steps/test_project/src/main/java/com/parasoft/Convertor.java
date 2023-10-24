package com.parasoft;

import com.parasoft.interfaces.IConvertor;

public class Convertor implements IConvertor {
    public String convertToString(int a) {
        return "" + a;
    }

    @Override
    public String name() {
        return "number to string convertor";
    }
}
