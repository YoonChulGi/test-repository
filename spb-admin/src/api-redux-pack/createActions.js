import {
  FETCH_LIST,
  CREATE,
  /*UPDATE, FETCH,*/ RESET,
  LOGIN,
  LOGOUT,
  UPLOAD_BANNER,
  FETCH_BANNER_LIST,
  RESET_BANNER,
  UPDATE_BANNER,
  DELETE_BANNER,
} from "./actionTypes";
import Api from "../Api";

export default (resourceName, key = "idx") => ({
  collection: (params = {}, meta = {}) => ({
    type: FETCH_LIST,
    promise: Api.get("/api/checkout", { params }),
    meta: {
      ...meta,
      key,
      resourceName,
    },
  }),
  bannerList: (params = {}, meta = {}) => ({
    type: FETCH_BANNER_LIST,
    promise: Api.get(resourceName, { params }),
    meta: {
      ...meta,
      key: "id",
      resourceName,
    },
  }),
  reset: () => ({
    type: RESET,
    meta: { resourceName },
  }),
  resetBanner: () => ({
    type: RESET_BANNER,
    meta: { resourceName },
  }),
  create: (data, params = {}, meta = {}) => ({
    type: CREATE,
    promise: Api.post(resourceName, data, { params }),
    meta: {
      ...meta,
      key: "id",
      resourceName,
    },
  }),
  login: (data, params = {}, meta = {}) => ({
    type: LOGIN,
    promise: Api.post(resourceName, data, { params }),
    meta: {
      ...meta,
      key: "email",
      resourceName,
    },
  }),
  logout: (meta = {}) => ({
    type: LOGOUT,
    promise: Api.get(resourceName),
    meta: {
      ...meta,
      key: "email",
      resourceName,
    },
  }),
  upload: (data, params = {}, meta = {}) => ({
    type: UPLOAD_BANNER,
    promise: Api.post(resourceName, data, { params }),
    meta: {
      ...meta,
      resourceName,
    },
  }),
  update: (data, params = {}, meta = {}) => ({
    type: UPDATE_BANNER,
    promise: Api.put(resourceName, data, { params }),
    meta: {
      ...meta,
      resourceName,
    },
  }),
  delBanner: (data, params = {}, meta = {}) => ({
    type: DELETE_BANNER,
    promise: Api.delete(resourceName, { data }, { params }),
    meta: {
      ...meta,
      resourceName,
    },
  }),
});
