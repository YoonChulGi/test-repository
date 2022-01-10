import { connect } from 'react-redux';
import TransactionSearchFilter from '../../components/main/TransactionSearchFilter';
import { requestTransactionList } from '../../actions/transasctionPackActions';
import { setFilter } from '../../actions/searchFilterActions';

export default connect(null, { requestTransactionList, setFilter })(TransactionSearchFilter);
