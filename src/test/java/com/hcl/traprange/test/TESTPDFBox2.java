/**
 * Copyright (C) 2015, GIAYBAC
 *
 * Released under the MIT license
 */
package com.hcl.traprange.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.hcl.traprange.PDFTableExtractor;
import com.hcl.traprange.TrapRangeBuilder;
import com.hcl.traprange.entity.Table;
import com.hcl.traprange.entity.TableRow;

import technology.tabula.CommandLineAppEx;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.simple.JSONArray;
import org.junit.Test;

/**
 *
 * @author THOQ LUONG Mar 21, 2015 11:23:40 PM
 */
public class TESTPDFBox2 extends PDFTextStripper {

	// --------------------------------------------------------------------------
	// Members
	private final List<Range<Integer>> ranges = new ArrayList<>();

	private final TrapRangeBuilder trapRangeBuilder = new TrapRangeBuilder();

	// --------------------------------------------------------------------------
	// Initialization and releasation
	public TESTPDFBox2() throws IOException {
		super.setSortByPosition(true);
	}

	// --------------------------------------------------------------------------
	// Getter N Setter
	// --------------------------------------------------------------------------
	// Method binding
	@Test
	public void test() throws IOException {
		String homeDirectory = System.getProperty("user.dir");
		String filePath = Paths.get(homeDirectory, "_Docs", "sample-1.pdf").toString();
		File pdfFile = new File(filePath);
		PDDocument pdDocument = PDDocument.load(pdfFile);
		// PrintTextLocations printer = new PrinTextLocations();
		PDPage page = pdDocument.getPage(0);

		this.processPage(page);
		// Print out all text
		Collections.sort(ranges, new Comparator<Range>() {
			@Override
			public int compare(Range o1, Range o2) {
				return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
			}
		});
		for (Range range : ranges) {
			System.out.println("> " + range);
		}
		// Print out all ranges
		List<Range<Integer>> trapRanges = trapRangeBuilder.build();
		for (Range trapRange : trapRanges) {
			System.out.println("TrapRange: " + trapRange);
		}
	}

	// --------------------------------------------------------------------------
	// Implement N Override
	@Override
	protected void processTextPosition(TextPosition text) {
		Range range = Range.closed((int) text.getY(), (int) (text.getY() + text.getHeight()));
		System.out.println("Text: " + text.getUnicode());
		trapRangeBuilder.addRange(range);
	}
	// --------------------------------------------------------------------------
	// Utils
	// --------------------------------------------------------------------------
	// Inner class

	@Test
	public void testExtractWordLocationsNotWorks() throws IOException, ParseException {
		extractContent("sample-12.pdf");
	}

	private void extractContent(String file)
			throws ParseException, IOException, JsonParseException, JsonMappingException, InvalidPasswordException {
		String homeDirectory = System.getProperty("user.dir");
		String sourceDirectory = Paths.get(homeDirectory, "_Docs").toString();
		try (PDDocument doc = PDDocument.load(new File(sourceDirectory + File.separator + file))) {

			JSONArray arr = getTableJson(doc);
			System.out.println(arr.toJSONString());

			PDFTableExtractor extractor = new PDFTableExtractor();
			
			int[] sl = buildIndex(doc);
			extractor.exceptLine(sl);

			List<Table> tables = extractor.extract(doc);

			for (Table table : tables) {
				List<TableRow> tr = table.getRows();
				System.out.println("tr.size()=" + tr.size());

				ArrayList<String[]> list1 = table.extract();

				for (String[] sts : list1) {
					for (String st : sts) {
						System.out.print(st + ",");
					}
					System.out.println();
				}
			}
		}
	}

	private int[] buildIndex(PDDocument doc) throws IOException {
		ArrayList<Integer> skipLines = new ArrayList<>();
		String startTable = "COMMODITIES:";
		String endTable = "";
		AtomicInteger line = new AtomicInteger();
		AtomicBoolean skipLine = new AtomicBoolean();
		ArrayList<TextPosition> list = new ArrayList<>();
		
		//PDFTextStripper stripper = new PDFTextStripper();
		PDFTextStripper stripper = new PDFTextStripper() {
			@Override
			protected void writeString(String text, List<TextPosition> textPositions) throws IOException {

				System.out.println(text);
				list.addAll(textPositions);
			}
		};
		//System.out.println(stripper.getText(doc));
		stripper.setSortByPosition(true);
		stripper.getText(doc);
		
		
		return null;
	}

	private JSONArray getTableJson(PDDocument pdfDocument2)
			throws ParseException, IOException, JsonParseException, JsonMappingException {
		try (StringWriter sw = new StringWriter()) {
			CommandLineAppEx cla = new CommandLineAppEx(sw);
			cla.extractFile(pdfDocument2);
			String data = sw.toString();
			ObjectMapper m = new ObjectMapper();
			JSONArray arr = m.readValue(data, JSONArray.class);
			return arr;
		}
	}
}
