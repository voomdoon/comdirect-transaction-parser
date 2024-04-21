package de.voomdoon.finance.budgetbook.parser.transaction.comdirect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		private record Result(List<BankStatementTransaction> data, Exception exception) {
		}

		/**
		 * @since 0.1.0
		 */
		private static final Map<String, Result> RESULTS = new HashMap<>();

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_read_endOfAccount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2018-03-01.pdf");

			assertThat(actuals).element(39).extracting(BankStatementTransaction::getAmount).isEqualTo(2182L);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_read_endOfAccountAtPageEnd() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2018-04-03.pdf");

			assertThat(actuals).element(29).extracting(BankStatementTransaction::getAmount).isEqualTo(-990L);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_read_endOfdFirstAccount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(44).extracting(BankStatementTransaction::getAmount).isEqualTo(-6820L);
		}

		/**
		 * @since 0.1.0
		 */
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
		void test_read_lastOfLastAccount() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(actuals.size() - 1).extracting(BankStatementTransaction::getAmount)
					.isEqualTo(25518L);
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
					.extracting(Reference::creditor).asString().startsWith("DE70ZZZ");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_reference_endToEnd() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getReference)
					.extracting(Reference::endToEnd).asString().startsWith("S/836522141100");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_reference_endToEnd_notSpecified() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(1).extracting(BankStatementTransaction::getReference).isNull();
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_reference_mandate() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(0).extracting(BankStatementTransaction::getReference)
					.extracting(Reference::mandate).asString().startsWith("M002");
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

			assertThat(actuals).element(1).extracting(BankStatementTransaction::getWhat).asString().startsWith("RF72X");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_what_removeLikeBreak_Einkauf() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(31).extracting(BankStatementTransaction::getWhat).asString()
					.contains("Einkauf");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_what_removeLikeBreak_length() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(28).extracting(BankStatementTransaction::getWhat).asString().contains("7027");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_value_what_withoutEndToEndRef() throws Exception {
			logTestStart();

			List<BankStatementTransaction> actuals = parseTransactions("Finanzreport_2017-06-02.pdf");

			assertThat(actuals).element(3).extracting(BankStatementTransaction::getWhat).asString()
					.startsWith("BARING");
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
			Result result = RESULTS.computeIfAbsent(input, key -> parseTransactionsInternal(input));

			if (result.exception() != null) {
				throw result.exception();
			}

			return result.data();
		}

		/**
		 * DOCME add JavaDoc for method parseTransactionsInternal
		 * 
		 * @param input
		 * @return
		 * @since DOCME add inception version number
		 */
		private Result parseTransactionsInternal(String input) {
			Path source = Path.of(
					"C:/workspaces/vd/public-finance/comdirect-transaction-parser-private-test-data/src/test/resources",
					input);
			Path target;

			try {
				target = Path.of(getTempDirectory().toString(), "input.pdf");
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO implement error handling
				throw new RuntimeException("Error at 'parseTransactionsInternal': " + e.getMessage(), e);
			}

			ComdirectTransactionParser parser = new ComdirectTransactionParser(target.toString());

			List<BankStatementTransaction> result = null;
			Exception exception = null;

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
				return new Result(null, exception);
			}

			logger.debug("result:\n\t" + result.stream().map(Object::toString).collect(Collectors.joining("\n\t")));

			return new Result(result, exception);
		}
	}
}
