package de.voomdoon.finance.budgetbook.parser.transaction.comdirect;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import de.voomdoon.finance.budgetbook.model.Account;
import de.voomdoon.finance.budgetbook.model.BankStatementTransaction;
import de.voomdoon.finance.budgetbook.model.Reference;
import de.voomdoon.logging.LogManager;
import de.voomdoon.logging.Logger;
import de.voomdoon.util.pdf.PdfGraphics;
import de.voomdoon.util.pdf.PdfReader;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class ComdirectTransactionParser {

	/**
	 * DOCME add JavaDoc for ComdirectTransactionParser
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	private record AccountsResult(List<Account> accounts, int yOverviewEnd) {

	}

	/**
	 * DOCME add JavaDoc for ComdirectTransactionParser
	 *
	 * @author André Schulz
	 * 
	 * @since 0.1.0
	 */
	private record LastEnd(int pageIndex, int y) {

	}

	/**
	 * @since 0.1.0
	 */
	private static final int HEIGHT = 841;

	/**
	 * @since 0.1.0
	 */
	private static final int WIDTH = 595;

	/**
	 * @since 0.1.0
	 */
	private static final int Y_MIN = 50;

	/**
	 * DOCME add JavaDoc for method translate
	 * 
	 * @param rectangle
	 * @param dx
	 * @param dy
	 * @return
	 * @since 0.1.0
	 * @deprecated TODO move to some awl util or find framework
	 */
	@Deprecated
	private static Rectangle translate(Rectangle rectangle, int dx, int dy) {
		return new Rectangle(rectangle.x + dx, rectangle.y + dy, rectangle.width, rectangle.height);
	}

	/**
	 * @since 0.1.0
	 */
	private final Logger logger = LogManager.getLogger(getClass());

	/**
	 * @since 0.1.0
	 */
	private PdfReader reader;

	/**
	 * @since 0.1.0
	 */
	private int yDebugOffset = 10;

	/**
	 * DOCME add JavaDoc for constructor ComdirectTransactionParser
	 * 
	 * @since 0.1.0
	 */
	public ComdirectTransactionParser(String fileName) {
		reader = new PdfReader(new File(fileName));
		debug(reader);
	}

	/**
	 * DOCME add JavaDoc for method parseTransactions
	 * 
	 * @return
	 * @since 0.1.0
	 */
	public List<BankStatementTransaction> parseTransactions() {
		List<BankStatementTransaction> result = new ArrayList<>();

		try {
			AccountsResult accounts = readAccounts(reader);
			logger.debug("accounts: " + accounts);
			addTransactionsFromAccounts(result, accounts);
		} catch (IOException e) {
			// TODO implement error handling
			throw new RuntimeException("Error at 'parseTransactions': " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * DOCME add JavaDoc for method saveDebug
	 * 
	 * @param fileName
	 * @throws IOException
	 * @since 0.1.0
	 */
	public void saveDebug(String fileName) throws IOException {
		reader.getDocument().save(new File(fileName));
	}

	/**
	 * DOCME add JavaDoc for method addTransactionsFromAccount
	 * 
	 * @param result
	 * @param account
	 * @param lastEnd
	 * @return
	 * @throws IOException
	 * @since 0.1.0
	 */
	private LastEnd addTransactionsFromAccount(List<BankStatementTransaction> result, Account account, LastEnd lastEnd)
			throws IOException {
		logger.debug("addTransactionsFromAccount " + account + " " + lastEnd);
		int yStart = findHeightFromTop(reader, lastEnd.pageIndex(), account.name(), lastEnd.y());
		yStart = findHeightFromTop(reader, lastEnd.pageIndex(), "Alter Saldo", yStart);
		logger.debug("yStart: " + yStart);

		LastEnd current = new LastEnd(lastEnd.pageIndex(), yStart);

		while (true) {
			// OPTIMIZE speed: cache result for same page
			int y = findHeightFromTop(reader, current.pageIndex, "Neuer Saldo", current.y());

			current = maybeAddTransaction(result, account, current, y);
			logger.debug("current: " + current);

			if (y > -1 && current.y() <= y) {
				logger.info("done with account " + account + " y=" + y + " (current: " + current + ")");
				break;
			}

			// if (current.pageIndex() == 3) {
			// logger.warn("DEBUG break");
			// break;
			// }
		}

		// TODO implement addTransactionsFromAccount
		return current;
	}

	/**
	 * DOCME add JavaDoc for method addTransactions
	 * 
	 * @param result
	 * @param accounts.a
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void addTransactionsFromAccounts(List<BankStatementTransaction> result, AccountsResult accounts)
			throws IOException {
		LastEnd lastEnd = new LastEnd(1, accounts.yOverviewEnd());

		for (Account account : accounts.accounts()) {
			// FIXME accounts are not in order of overview
			if (account.name().equals("Depot")) {
				continue;
			}

			lastEnd = addTransactionsFromAccount(result, account, lastEnd);
		}
	}

	/**
	 * DOCME add JavaDoc for method debug
	 * 
	 * @param reader
	 * @since 0.1.0
	 */
	private void debug(PdfReader reader) {
		try {
			PDDocument document = reader.getDocument();

			debugPages(document);

			// debugPageContent(reader, 1);
		} catch (Exception e) {
			// TODO implement error handling
			throw new RuntimeException("Error at 'debugPageContent': " + e.getMessage(), e);
		}
	}

	/**
	 * DOCME add JavaDoc for method debugPageContent
	 * 
	 * @param reader
	 * @param pageIndex
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void debugPageContent(PdfReader reader, int pageIndex) throws IOException {
		PDPage page = reader.getDocument().getPage(pageIndex);

		PDFTextStripperByArea stripper = new PDFTextStripperByArea();

		String regionName = "debug-area";
		StringBuilder sb = new StringBuilder();
		sb.append("content of page ").append(pageIndex).append(":");

		for (int y = HEIGHT; y >= 0; y--) {
			stripper.addRegion(regionName, new Rectangle(0, y, 595, 1));
			stripper.extractRegions(page);

			String result = stripper.getTextForRegion(regionName);

			if (result.endsWith("\r\n")) {
				result = result.substring(0, result.length() - 2);
			}

			sb.append("\n").append(y).append(": '").append(result).append("'");
		}

		logger.debug(sb.toString());
	}

	/**
	 * DOCME add JavaDoc for method debugPages
	 * 
	 * @param document
	 * @since 0.1.0
	 */
	private void debugPages(PDDocument document) {
		Iterator<PDPage> iterator = document.getPages().iterator();
		int iPage = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("pages:");

		while (iterator.hasNext()) {
			PDPage page = iterator.next();
			// sb.append("\npage ").append(iPage++);
			// sb.append("\n\tmediaBox: ").append(page.getMediaBox());
		}

		logger.debug(sb.toString());
	}

	/**
	 * DOCME add JavaDoc for method searchHeight
	 * 
	 * @param reader
	 * @param pageIndex
	 * @param string
	 * @return
	 * @throws IOException
	 * @since 0.1.0
	 */
	private int findHeightFromTop(PdfReader reader, int pageIndex, String string) {
		return findHeightFromTop(reader, pageIndex, string, HEIGHT);
	}

	/**
	 * DOCME add JavaDoc for method findHeight
	 * 
	 * @param reader
	 * @param pageIndex
	 * @param string
	 * @param yFrom
	 * @return
	 * @throws IOException
	 * @since 0.1.0
	 */
	private int findHeightFromTop(PdfReader reader, int pageIndex, String string, int yFrom) {
		return findHeightFromTop(reader, pageIndex, string, 0, WIDTH, yFrom);
	}

	/**
	 * DOCME add JavaDoc for method findHeight
	 * 
	 * @param reader
	 * @param pageIndex
	 * @param content
	 * @param yFrom
	 * @return
	 * @since 0.1.0
	 */
	private int findHeightFromTop(PdfReader reader, int pageIndex, String content, int x0, int x1, int yFrom) {
		// TODO use optional
		StringBuilder sb = new StringBuilder();
		sb.append("findHeightFromTop y=").append(yFrom).append(" '").append(content).append("' @").append(pageIndex)
				.append(":");

		// OPTIMIZE speed: maybe extract all rows at once
		for (int y = yFrom; y >= Y_MIN; y--) {
			Rectangle rectangle = new Rectangle(x0, y, x1 - x0, 1);
			String result = reader.readText(pageIndex, rectangle);

			if (!result.isEmpty()) {
				sb.append("\n").append(y).append(": '").append(result).append("'");
			}

			if (result.contains(content)) {
				highlightMatch(pageIndex, rectangle);
				logger.trace(sb.toString());
				return y;
			}
		}

		logger.trace(sb.toString());

		return -1;
	}

	/**
	 * DOCME add JavaDoc for method highlightMatch
	 * 
	 * @param pageIndex
	 * @param rectangle
	 * @since 0.1.0
	 */
	private void highlightMatch(int pageIndex, Rectangle rectangle) {
		PdfGraphics graphics = PdfGraphics.create(reader.getDocument(), reader.getDocument().getPage(pageIndex));
		graphics.setAlpha(0.25F);
		graphics.setStrokingColor(Color.GREEN);
		graphics.drawRectangle(translate(rectangle, 0, yDebugOffset));
		graphics.close();
	}

	/**
	 * DOCME add JavaDoc for method addTransaction
	 * 
	 * @param result
	 * @param account
	 * @param lastEnd
	 * @param yBottom
	 * @return
	 * @since 0.1.0
	 */
	private LastEnd maybeAddTransaction(List<BankStatementTransaction> result, Account account, LastEnd lastEnd,
			int yBottom) {
		logger.debug("maybeAddTransaction " + lastEnd + " yBottom=" + yBottom);

		// OPTIMIZE speed: increase initial search offset
		// TODO move yLeft to right most position

		BankStatementTransaction transaction = new BankStatementTransaction();

		int xDateRight = 110;

		int yBookingDate = findHeightFromTop(reader, lastEnd.pageIndex(), ".", 0, xDateRight, lastEnd.y() - 1);

		if (yBookingDate < yBottom) {
			logger.debug("bottom reaced");
			return new LastEnd(lastEnd.pageIndex(), yBottom);
		}

		String bookingDate = readText(lastEnd.pageIndex(), new Rectangle(0, yBookingDate, xDateRight, 1));
		logger.debug("booking date: '" + bookingDate + "' @y=" + yBookingDate);
		transaction.setBookingDate(parseLocalDate(bookingDate));

		int yValuta = findHeightFromTop(reader, lastEnd.pageIndex(), ".", 0, xDateRight, yBookingDate - 1);
		String valuta = readText(lastEnd.pageIndex(), new Rectangle(0, yValuta, xDateRight, 1));
		logger.debug("valuta: '" + valuta + "' @y=" + yValuta);
		transaction.setValuta(parseLocalDate(valuta));

		int yNext = findHeightFromTop(reader, lastEnd.pageIndex(), ".", 0, xDateRight, yValuta - 1);
		String next = readText(lastEnd.pageIndex(), new Rectangle(0, yNext, xDateRight, 1));
		logger.debug("next: '" + next + "' @y=" + yNext);
		boolean last = false;

		if (yNext == -1) {
			logger.debug("last transaction of page reached");
			yNext = 60;
			last = true;
		} else if (yNext < yBottom) {
			logger.debug("adjusting yNext to yBottom=" + yBottom);
			yNext = yBottom;
		}

		int height = yBookingDate - yNext;

		int xOtherAccountLeft = 185;
		int xOtherAccountRight = 300;

		String otherAccount = readText(lastEnd.pageIndex(),
				new Rectangle(xOtherAccountLeft, yNext + 1, xOtherAccountRight - xOtherAccountLeft, height));
		otherAccount = removeLineBreaks(otherAccount);
		logger.debug("otherAccount: '" + otherAccount + "'");
		transaction.setOtherAccount(otherAccount);

		int xDetailsLeft = 300;
		int xDetailsRight = 500;
		String details = readText(lastEnd.pageIndex(),
				new Rectangle(xDetailsLeft, yNext + 1, xDetailsRight - xDetailsLeft, height));
		logger.trace("details: '" + details + "'");

		processDetails(details, transaction);

		int xAmountLeft = 500;
		int xAmountRight = 560;
		String amount = readText(lastEnd.pageIndex(),
				new Rectangle(xAmountLeft, yNext + 1, xAmountRight - xAmountLeft, height));
		amount = removeLineBreaks(amount);
		logger.debug("amount: '" + amount + "'");
		transaction.setAmount(parseAmount(amount));

		result.add(transaction);

		if (last) {
			logger.info("page end reached");
			return new LastEnd(lastEnd.pageIndex() + 1, HEIGHT);

		}

		return new LastEnd(lastEnd.pageIndex(), yNext + 1);
	}

	private long parseAmount(String amount) {
		return Long.parseLong(amount.replace(",", "").replace(".", ""));
	}

	/**
	 * DOCME add JavaDoc for method parseLocalDate
	 * 
	 * @param value
	 * @return
	 * @since 0.1.0
	 */
	private LocalDate parseLocalDate(String value) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		return LocalDate.parse(value, formatter);
	}

	/**
	 * DOCME add JavaDoc for method procesDetails
	 * 
	 * @param details
	 * @param transaction
	 * @since 0.1.0
	 */
	private void processDetails(String details, BankStatementTransaction transaction) {
		int endToEndRefIndex = details.indexOf("End-to-End-Ref.:");

		if (endToEndRefIndex == -1) {
			logger.debug("endToEndRefIndex: " + endToEndRefIndex);
			transaction.setWhat(details);

			return;
		} else {
			transaction.setWhat(details.substring(0, endToEndRefIndex).trim());
		}

		int mandateIndex = details.indexOf("CORE / Mandatsref.:");

		if (mandateIndex == -1) {
			logger.debug("mandateIndex: " + mandateIndex);
			return;
		}

		String endToEnd = details.substring(endToEndRefIndex + "End-to-End-Ref.:".length(), mandateIndex).trim();
		logger.debug("endToEnd: " + endToEnd);

		int creditorIndex = details.indexOf("Gläubiger-ID:");

		if (creditorIndex == -1) {
			logger.debug("creditorIndex: " + creditorIndex);
			return;
		}

		String mandate = details.substring(mandateIndex + "CORE / Mandatsref.:".length(), creditorIndex).trim();
		logger.debug("mandate: " + mandate);

		String creditor = details.substring(creditorIndex + "Gläubiger-ID:".length()).trim();
		logger.debug("creditor: " + creditor);

		transaction.setReference(new Reference(endToEnd, mandate, creditor));
	}

	/**
	 * DOCME add JavaDoc for method getAccounts
	 * 
	 * @param reader
	 * 
	 * @return
	 * @throws IOException
	 * 
	 * @since 0.1.0
	 */
	private AccountsResult readAccounts(PdfReader reader) throws IOException {
		int yStartOverview = findHeightFromTop(reader, 1, "Kontoübersicht");
		logger.debug("Kontoübersicht: at y=" + yStartOverview);

		int yStartCurrency = findHeightFromTop(reader, 1, "Kontowährung", yStartOverview);
		logger.debug("Kontowährung: at y=" + yStartCurrency);

		int yEnd = findHeightFromTop(reader, 1, "Gesamtsaldo", yStartCurrency);
		logger.debug("Gesamtsaldo: at y=" + yEnd);
		// TODO implement getAccounts

		int xNameRight = 200;
		int xIbanRight = 400;

		List<Account> result = new ArrayList<>();

		for (int y = yStartCurrency - 1; y > yEnd; y--) {
			String name = readText(1, new Rectangle(0, y, xNameRight, 1));
			String iban = readText(1, new Rectangle(xNameRight, y, xIbanRight - xNameRight, 1));

			if (!name.isEmpty()) {
				result.add(new Account(name, iban.replace(" ", "")));
			}
		}

		return new AccountsResult(result, yEnd);
	}

	/**
	 * DOCME add JavaDoc for method readText
	 * 
	 * @param pageIndex
	 * @param rectangle
	 * @return
	 * @since 0.1.0
	 */
	private String readText(int pageIndex, Rectangle rectangle) {
		PdfGraphics graphics = PdfGraphics.create(reader.getDocument(), reader.getDocument().getPage(pageIndex));
		graphics.setAlpha(0.25F);
		graphics.setStrokingColor(Color.RED);
		graphics.drawRectangle(translate(rectangle, 0, yDebugOffset));
		graphics.close();

		String result = reader.readText(pageIndex, rectangle);

		logger.debug("readText " + rectangle + " : '" + result + "'");

		return result;
	}

	/**
	 * DOCME add JavaDoc for method removeLineBreaks
	 * 
	 * @param value
	 * @return
	 * @since 0.1.0
	 */
	private String removeLineBreaks(String value) {
		return value.replace("\r\n", " ");
	}
}
