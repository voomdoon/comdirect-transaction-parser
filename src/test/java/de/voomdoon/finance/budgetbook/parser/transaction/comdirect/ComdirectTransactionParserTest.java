package de.voomdoon.finance.budgetbook.parser.transaction.comdirect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.voomdoon.finance.budgetbook.model.BankStatementTransaction;
import de.voomdoon.finance.budgetbook.model.Reference;
import de.voomdoon.testing.tests.TestBase;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
class ComdirectTransactionParserTest {

	/**
	 * DOCME add JavaDoc for ComdirectTransactionParserTest
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	class ParseTransactionsTest extends TestBase {

		/**
		 * @since 0.1.0
		 */
		@Disabled
		@Test
		void test_nextPage() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(8).extracting(BankStatementTransaction::getBookingDate)
					.isEqualTo(LocalDate.of(2017, 5, 9));
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_secondRow() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(1).extracting(BankStatementTransaction::getBookingDate)
					.isEqualTo(LocalDate.of(2017, 5, 5));
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_amount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getAmount).isEqualTo(-3700L);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_bookingDate() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getBookingDate)
					.isEqualTo(LocalDate.of(2017, 5, 4));
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_otherAccount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getOtherAccount)
					.isEqualTo("VATTENFALL EUROPE SALES");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_reference_creditor() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getReference)
					.extracting(Reference::creditor).isEqualTo("DE70ZZZ00000119765");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_reference_endToEnd() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getReference)
					.extracting(Reference::endToEnd).isEqualTo("S/836522141100/601904279283");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_reference_mandate() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getReference)
					.extracting(Reference::mandate).isEqualTo("M002000001082336");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_valuta() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getValuta)
					.isEqualTo(LocalDate.of(2017, 5, 4));
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_what() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(1).extracting(BankStatementTransaction::getWhat).isEqualTo("RF72X114211565");
		}

		/**
		 * DOCME add JavaDoc for method run
		 * 
		 * @param input
		 * @return
		 * @throws IOException
		 * @since 0.1.0
		 */
		private List<BankStatementTransaction> parseTransactions(String input) throws IOException {
			Path source = Path.of(
					"C:/workspaces/vd/public-finance/comdirect-transaction-parser-private-test-data/src/test/resources",
					input);
			Path target = Path.of(getTempDirectory().toString(), "input.pdf");
			Files.copy(source, target);

			ComdirectTransactionParser parser = new ComdirectTransactionParser(target.toString());

			List<BankStatementTransaction> result = parser.parseTransactions();

			try {
				parser.saveDebug(
						"C:/workspaces/vd/public-finance/comdirect-transaction-parser-private-test-data/src/test/resources/"
								+ input.substring(0, input.lastIndexOf('.')) + "_parsing.pdf");
			} catch (Exception e) {
				logger.warn("Failed to save debug file: " + e.getMessage());
			}

			return result;
		}
	}
}
