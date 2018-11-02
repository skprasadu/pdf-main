/**
 * Copyright (C) 2015, GIAYBAC
 *
 * Released under the MIT license
 */
package com.hcl.traprange.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

import com.hcl.traprange.PDFTableExtractor;
import com.hcl.traprange.entity.Table;
import com.hcl.traprange.entity.TableRow;

/**
 *
 * @author THOQ LUONG Mar 22, 2015 5:36:40 PM
 */
public class TestExtractor1 {

	// --------------------------------------------------------------------------
	// Members
	// --------------------------------------------------------------------------
	// Initialization and releasation
	// --------------------------------------------------------------------------
	// Getter N Setter
	// --------------------------------------------------------------------------
	// Method binding
	@Test
	public void test() throws IOException {
		PropertyConfigurator.configure(TestExtractor1.class.getResource("/com/giaybac/traprange/log4j.properties"));

		String homeDirectory = System.getProperty("user.dir");

		String sourceDirectory = Paths.get(homeDirectory, "_Docs").toString();
		String resultDirectory = Paths.get(homeDirectory, "_Docs", "result").toString();

		try (PDDocument document = PDDocument.load(new File(sourceDirectory + File.separator + "sample-11.pdf"))) {
			PDFTableExtractor extractor = new PDFTableExtractor();

			extractor.exceptLine(new int[] { 0, 1, 2, 3, 4, 5, 6, 16, 17 });

			List<Table> tables = extractor.extract(document);
			// try (Writer writer = new OutputStreamWriter(new
			// FileOutputStream(resultDirectory + "//sample-11.html"),
			// "UTF-8")) {
			for (Table table : tables) {
				List<TableRow> tr = table.getRows();
				System.out.println("tr.size()=" + tr.size());

				ArrayList<String[]> list = table.extract();

				for (String[] sts : list) {
					for (String st : sts) {
						System.out.print(st + ",");
					}
					System.out.println();
				}
			}
		}
	}
	// --------------------------------------------------------------------------
	// Implement N Override
	// --------------------------------------------------------------------------
	// Utils
	// --------------------------------------------------------------------------
	// Inner class
}
