import createSelectors from "../api-redux-pack/createSelectors";

export const { createLoadingStateSelector: adminCreateLoadingStateSelector } =
  createSelectors("api/addAdmin");
