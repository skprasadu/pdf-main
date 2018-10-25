package technology.tabula;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import net.sourceforge.tess4j.TesseractException;

public class CommandLineAppEx extends CommandLineApp {
	
    public CommandLineAppEx(Appendable defaultOutput) throws ParseException {
        super();
        this.defaultOutput = defaultOutput;
        this.pageAreas = null;
        this.pages = null;
        this.outputFormat = OutputFormat.JSON;
        this.tableExtractor = new TableExtractor();
        this.tableExtractor.setMethod(ExtractionMethod.SPREADSHEET);
    }

    public void extractFile(PDDocument pdfDocument) throws ParseException, IOException {
    	extractFile(pdfDocument, defaultOutput);
    }

    private void extractFile(PDDocument pdfDocument, Appendable outFile) throws IOException {
		PageIterator pageIterator = getPageIterator(pdfDocument);
		List<Table> tables = new ArrayList<>();

		while (pageIterator.hasNext()) {
		    Page page = pageIterator.next();

		    if (pageAreas != null) {
		        for (Pair<Integer, Rectangle> areaPair : pageAreas) {
		            Rectangle area = areaPair.getRight();
		            if (areaPair.getLeft() == RELATIVE_AREA_CALCULATION_MODE) { 
		                area  = new Rectangle((float) (area.getTop() / 100 * page.getHeight()),
		                        (float) (area.getLeft() / 100 * page.getWidth()), (float) (area.getWidth() / 100 * page.getWidth()),
		                        (float) (area.getHeight() / 100 * page.getHeight()));                            
		            }
		            tables.addAll(tableExtractor.extractTables(page.getArea(area)));
		        }
		    } else {
		        tables.addAll(tableExtractor.extractTables(page));
		    }
		}
		writeTables(tables, outFile);
	}
    
    public static void main(String[] args) {
        try(PDDocument doc = PDDocument.load(new File(args[0]))){
            ObjectExtractor o = new ObjectExtractor(doc);
            String layout = IOUtils.toString(new FileInputStream(args[1]));
            System.out.println(o.extractJson(layout));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
