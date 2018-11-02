/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.traprange.test;

import org.junit.Test;

import com.hcl.traprange.entity.Table;
import com.hcl.traprange.entity.TableCell;
import com.hcl.traprange.entity.TableRow;

/**
 *
 * @author thoqbk
 */
public class TestTableRow {

    @Test
    public void doTest() {
        TableRow row = new TableRow(0);

        TableCell cell0 = new TableCell(0, "");
        row.getCells().add(cell0);
        
        TableCell cell1 = new TableCell(1, "2");
        row.getCells().add(cell1);

        TableCell cell2 = new TableCell(2, "Vietnam");
        row.getCells().add(cell2);

        System.out.println("Row: " + row);

        Table table = new Table(0, 3);
        table.getRows().add(row);

        System.out.println("Table: " + table);

    }
}
