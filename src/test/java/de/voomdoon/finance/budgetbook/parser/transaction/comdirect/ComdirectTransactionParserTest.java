package de.voomdoon.finance.budgetbook.parser.transaction.comdirect;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
		private static Exception exception;

		/**
		 * @since 0.1.0
		 */
		private static List<BankStatementTransaction> result;

		/**
		 * @since 0.1.0
		 */
		@Disabled
		@Test
		void test_read_endOfdFirstAccount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(actuals.size() - 1).extracting(BankStatementTransaction::getAmount)
					.isEqualTo(-6820L);
		}

		/**
		 * @since 0.1.0
		 */
		@Disabled
		@Test
		void test_read_lastOfFirstAccountPage() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(7).extracting(BankStatementTransaction::getAmount).isEqualTo(-3991L);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_read_nextPageOfFistAccount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(8).extracting(BankStatementTransaction::getWhat).asString().startsWith("BGF");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_read_secondRow() throws Exception {
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
		 * @throws Exception
		 * @since 0.1.0
		 */
		private List<BankStatementTransaction> parseTransactions(String input) throws Exception {
			// TODO add explicit cache
			if (result != null) {
				return result;
			} else if (exception != null) {
				throw exception;
			}

			Path source = Path.of(
					"C:/workspaces/vd/public-finance/comdirect-transaction-parser-private-test-data/src/test/resources",
					input);
			Path target = Path.of(getTempDirectory().toString(), "input.pdf");
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

			ComdirectTransactionParser parser = new ComdirectTransactionParser(target.toString());

			exception = null;

			try {
				result = parser.parseTransactions();
			} catch (Exception e) {
				exception = e;
			}

			try {
				parser.saveDebug(
						"C:/workspaces/vd/public-finance/comdirect-transaction-parser-private-test-data/src/test/resources/"
								+ input.substring(0, input.lastIndexOf('.')) + "_parsing.pdf");
			} catch (Exception e) {
				logger.warn("Failed to save debug file: " + e.getMessage());
			}

			if (exception != null) {
				throw exception;
			}

			return result;
		}
	}
}