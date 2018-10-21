package technology.tabula;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfextract.util.Util;

import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ObjectExtractor {

	private final PDDocument pdfDocument;

	public ObjectExtractor(PDDocument pdfDocument) {
		System.out.println("****ObjectExtractor****");
		this.pdfDocument = pdfDocument;
	}

	protected Page extractPage(Integer pageNumber) throws IOException {

		if (pageNumber > this.pdfDocument.getNumberOfPages() || pageNumber < 1) {
			throw new java.lang.IndexOutOfBoundsException("Page number does not exist");
		}

		PDPage p = this.pdfDocument.getPage(pageNumber - 1);

		ObjectExtractorStreamEngine se = new ObjectExtractorStreamEngine(p);
		se.processPage(p);

		TextStripper pdfTextStripper = new TextStripper(this.pdfDocument, pageNumber);

		pdfTextStripper.process();

		Utils.sort(pdfTextStripper.textElements, Rectangle.ILL_DEFINED_ORDER);

		float w, h;
		int pageRotation = p.getRotation();
		if (Math.abs(pageRotation) == 90 || Math.abs(pageRotation) == 270) {
			w = p.getCropBox().getHeight();
			h = p.getCropBox().getWidth();
		} else {
			w = p.getCropBox().getWidth();
			h = p.getCropBox().getHeight();
		}

		return new Page(0, 0, w, h, pageRotation, pageNumber, p, pdfTextStripper.textElements, se.rulings,
				pdfTextStripper.minCharWidth, pdfTextStripper.minCharHeight, pdfTextStripper.spatialIndex);
	}

	public PageIterator extract(Iterable<Integer> pages) {
		return new PageIterator(this, pages);
	}

	public PageIterator extract() {
		return extract(Utils.range(1, this.pdfDocument.getNumberOfPages() + 1));
	}

	public Page extract(int pageNumber) {
		return extract(Utils.range(pageNumber, pageNumber + 1)).next();
	}

	public void close() throws IOException {
		this.pdfDocument.close();
	}

	public String extractCsv(String layout) throws IOException, TesseractException {
		System.out.println("&&***********layout***************&&" + layout);
		try (StringWriter sw = new StringWriter()) {
			if (!isSearchablePdf(pdfDocument)) {
				UUID uuid = UUID.randomUUID();

				generateSearchablePdf(uuid, pdfDocument);
				try(PDDocument pd = PDDocument.load(new File("tmp/"+ uuid + "_out.pdf"))){
					return extractCsvData(pd, layout, sw);
				}
			} else {
				return extractCsvData(pdfDocument, layout, sw);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private String extractCsvData(PDDocument pdfDocument2, String layout, StringWriter sw)
			throws ParseException, IOException, JsonParseException, JsonMappingException {
		JSONArray arr = getTableJson(pdfDocument2, sw);

		return Util.extractCsvFromPdfExtract(pdfDocument2, arr, layout);
	}

	private void generateSearchablePdf(UUID uuid, PDDocument pdfDocument2) throws TesseractException, IOException {
		pdfDocument2.save(new File("tmp/" + uuid + "_in.pdf"));

		Tesseract instance = new Tesseract();

		List<RenderedFormat> list = new ArrayList<RenderedFormat>();
		list.add(RenderedFormat.PDF);

		instance.createDocuments("tmp/" + uuid + "_in.pdf", "tmp/" + uuid + "_out", list);
		System.out.println("converted");
	}

	public String extractJson(String layout) throws TesseractException {
		System.out.println("&&***********layout***************&&" + layout);
		try (StringWriter sw = new StringWriter()) {
			if (!isSearchablePdf(pdfDocument)) {
				UUID uuid = UUID.randomUUID();

				generateSearchablePdf(uuid, pdfDocument);
				try(PDDocument pd = PDDocument.load(new File("tmp/"+ uuid + "_out.pdf"))){
					//PDFTextStripper pdfStripper = new PDFTextStripper();
					//System.out.println( pdfStripper.getText(pd));

					return extractJsonData(pd, layout, sw);
				}
			} else {
				return extractJsonData(pdfDocument, layout, sw);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String extractJsonData(PDDocument pdfDocument2, String layout, StringWriter sw)
			throws ParseException, IOException, JsonParseException, JsonMappingException {
		JSONArray arr = getTableJson(pdfDocument2, sw);

		return Util.extractJsonFromPdfExtract(pdfDocument2, arr, layout);
	}

	private JSONArray getTableJson(PDDocument pdfDocument2, StringWriter sw)
			throws ParseException, IOException, JsonParseException, JsonMappingException {
		CommandLineAppEx cla = new CommandLineAppEx(sw);
		cla.extractFile(pdfDocument2);
		String data = sw.toString();
		System.out.println("data=" + data);
		ObjectMapper m = new ObjectMapper();
		JSONArray arr = m.readValue(data, JSONArray.class);
		return arr;
	}

	public static boolean isSearchablePdf(PDDocument doc) throws IOException {
		String parsedText = "";

		PDFTextStripper pdfStripper = new PDFTextStripper();
		parsedText = pdfStripper.getText(doc);

		//System.out.println("parsedText=" + parsedText.trim());
		return !parsedText.trim().isEmpty();
	}

}
