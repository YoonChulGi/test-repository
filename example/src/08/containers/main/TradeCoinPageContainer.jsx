import { connect } from 'react-redux';
import TradeCoinPage from '../../components/main/TradeCoinPage';
// import { createTransaction } from '../../actions/transactionActions';
import { createTransaction } from '../../actions/transasctionPackActions';

export default connect(null, { createTransaction })(TradeCoinPage);
