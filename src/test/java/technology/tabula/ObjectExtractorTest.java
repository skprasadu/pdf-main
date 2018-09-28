package technology.tabula;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

import net.sourceforge.tess4j.TesseractException;

public class ObjectExtractorTest {

	@Test
	public void testExtractCsvString() throws IOException, TesseractException {
		try (InputStream is = ObjectExtractorTest.class.getResourceAsStream("/ccl1-layout.json");
				InputStream is1 = ObjectExtractorTest.class.getResourceAsStream("/CCL1.pdf");
				PDDocument pdfDocument = PDDocument.load(is1)) {

			ObjectExtractor oe = new ObjectExtractor(pdfDocument);
			String layout = IOUtils.toString(is);
			oe.extractCsv(layout);
		}
	}

	@Test
	public void testExtractJsonString() throws IOException, TesseractException {
		try (InputStream is = ObjectExtractorTest.class.getResourceAsStream("/ccl1-layout.json");
				InputStream is1 = ObjectExtractorTest.class.getResourceAsStream("/CCL1.pdf");
				PDDocument pdfDocument = PDDocument.load(is1)) {

			ObjectExtractor oe = new ObjectExtractor(pdfDocument);
			String layout = IOUtils.toString(is);
			oe.extractJson(layout);
		}
	}
}
