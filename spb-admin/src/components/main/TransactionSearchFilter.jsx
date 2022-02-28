import React, { PureComponent } from "react";
import PropTypes from "prop-types";

import InlineList from "../../ui/InlineList";
import Button from "../../ui/Button";
import Text from "../../ui/Text";
import Input from "../../ui/Input";
import Form from "../../ui/Form";

import Select, { Option } from "../../ui/Select";

class TransactionSearchFilter extends PureComponent {
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
  }
  handleSubmit(params) {
    const { requestTransactionList, setFilter } = this.props;
    const cleanedParams = Object.entries(params)
      .filter(([key, value]) => value !== "")
      .reduce((obj, [key, value]) => ({ ...obj, [key]: value }), {});
    requestTransactionList(cleanedParams);
    setFilter(cleanedParams);
  }
  render() {
    return (
      <Form onSubmit={this.handleSubmit}>
        <Form.Consumer>
          {({ onChange, values }) => (
            <InlineList spacingBetween={2} verticalAlign="bottom">
              <Text xlarge bold>
                검색
              </Text>
              <Select
                name="searchField"
                label="검색 범위"
                onChange={onChange}
                value={values["searchField"]}
              >
                <Option label="선택해주세요" value="ALL" />
                <Option label="거래번호" value="idx" />
                <Option label="구매자" value="full_name" />
                <Option label="품목 ID" value="prd_ids" />
              </Select>
              <Input
                name="query"
                label="검색어를 입력하세요"
                onChange={onChange}
                value={values["query"]}
              />
              <Input
                name="from"
                label="거래 일시 from"
                onChange={onChange}
                value={values["from"]}
              />
              <Input
                name="to"
                label="거래 일시 to"
                onChange={onChange}
                value={values["to"]}
              />
              <Button type="submit" primary>
                검색
              </Button>
            </InlineList>
          )}
        </Form.Consumer>
      </Form>
    );
  }
}

TransactionSearchFilter.propTypes = {
  setTransactionList: PropTypes.func,
  setFilter: PropTypes.func,
};

export default TransactionSearchFilter;
