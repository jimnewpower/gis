package com.primalimited.gis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBFieldTest {

    @Test
    void jsonTest() {
        DBField dbField = new DBField("City", "String", "Denver");
        assertEquals("StringData{name='City', type='String', value='Denver'}", dbField.toJSON());

        dbField = new DBField("Population", "Long", 1500328);
        assertEquals("NumericData{name='Population', type='Long', value=1500328}", dbField.toJSON());

        dbField = new DBField("Velocity", "Float", 32.84);
        assertEquals("NumericData{name='Velocity', type='Float', value=32.84}", dbField.toJSON());
    }
}