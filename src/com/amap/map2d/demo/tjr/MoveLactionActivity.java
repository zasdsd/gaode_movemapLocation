package com.amap.map2d.demo.tjr;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnCameraChangeListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.amap.map2d.demo.R;

public class MoveLactionActivity extends Activity implements LocationSource,
		AMapLocationListener, OnCameraChangeListener, OnGeocodeSearchListener,
		OnPoiSearchListener {

	private AMap aMap;
	private MapView mapView;
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	// 设置不显示zoom
	private UiSettings mUiSettings;

	// 地图上标记
	// private Marker regeoMarker;
	// 这个是地理反编译
	// private GeocodeSearch geocoderSearch;

	// 查询
	private Query query;
	private PoiSearch poiSearch;

	private ListView lvList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_location);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		lvList = (ListView) findViewById(R.id.lvList);
		init();
	}

	// /**
	// * 响应逆地理编码
	// */
	// public void getAddress(final LatLonPoint latLonPoint) {
	// RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
	// GeocodeSearch.AMAP);//
	// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
	// geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	// }

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			mUiSettings = aMap.getUiSettings();
			mUiSettings.setZoomControlsEnabled(false);// 不显示zoom按钮
			setUpMap();
		}
	}

	// private void setUpMarket() {
	// regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
	// .icon(BitmapDescriptorFactory
	// .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
	// }

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.TRANSPARENT);//
		// 设置圆形的填充颜色
		// myLocationStyle.anchor(1, 1);// 设置小蓝点的锚点
		myLocationStyle.strokeWidth(0.0f);// 设置圆形的边框粗细
		aMap.setMyLocationStyle(myLocationStyle);

		aMap.setLocationSource(this);// 设置定位监听
		aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// aMap.setMyLocationType()
		aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
		// 这里是地理位置搜索
		// geocoderSearch = new GeocodeSearch(this);
		// geocoderSearch.setOnGeocodeSearchListener(this);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				if (mlocationClient.isStarted()) {
					mlocationClient.stopLocation();
				}
				LatLonPoint llp = new LatLonPoint(amapLocation.getLatitude(),
						amapLocation.getLongitude());
				// getAddress(llp);
				querySreach(llp, amapLocation.getCity());
				Log.e("AmapErr", "定位 " + amapLocation.getLongitude() + ": "
						+ amapLocation.getLatitude());
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
				Log.e("AmapErr", errText);
			}
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		Log.e("AmapErr", "activate OnLocationChangedListener");
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mlocationClient.setLocationListener(this);
			// 设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			// 设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mlocationClient.startLocation();
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
		LatLonPoint llp = new LatLonPoint(cameraPosition.target.latitude,
				cameraPosition.target.longitude);
		// getAddress(llp);
		querySreach(llp, null);
	}

	// /**
	// * marker点击时跳动一下
	// */
	// public void jumpPoint(final Marker marker) {
	// final Handler handler = new Handler();
	// final long start = SystemClock.uptimeMillis();
	// Projection proj = aMap.getProjection();
	// Point startPoint = proj.toScreenLocation(Constants.XIAN);
	// startPoint.offset(0, -100);
	// final LatLng startLatLng = proj.fromScreenLocation(startPoint);
	// final long duration = 1500;
	//
	// final BounceInterpolator interpolator = new BounceInterpolator();
	// handler.post(new Runnable() {
	// @Override
	// public void run() {
	// long elapsed = SystemClock.uptimeMillis() - start;
	// float t = interpolator.getInterpolation((float) elapsed
	// / duration);
	// double lng = t * Constants.XIAN.longitude + (1 - t)
	// * startLatLng.longitude;
	// double lat = t * Constants.XIAN.latitude + (1 - t)
	// * startLatLng.latitude;
	// marker.setPosition(new LatLng(lat, lng));
	// if (t < 1.0) {
	// handler.postDelayed(this, 16);
	// }
	// }
	// });
	// }

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// ToastUtil.show(MoveLactionActivity.this, "onGeocodeSearched");
	}

	/**
	 * 这里是坐标转地名
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				// aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				// AMapUtil.convertToLatLng(latLonPoint), 15));
				// regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
				// ToastUtil.show(MoveLactionActivity.this, result
				// .getRegeocodeAddress().getFormatAddress());
			} else {
				// ToastUtil.show(MoveLactionActivity.this, R.string.no_result);
			}
		} else if (rCode == 27) {
			// ToastUtil.show(MoveLactionActivity.this, R.string.error_network);
		} else if (rCode == 32) {
			// ToastUtil.show(MoveLactionActivity.this, R.string.error_key);
		} else {
			// ToastUtil.show(MoveLactionActivity.this,
			// getString(R.string.error_other) + rCode);
		}
	}

	private void querySreach(final LatLonPoint latLonPoint, final String city) {
		query = new PoiSearch.Query("", "", null);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query.setPageSize(10);// 设置每页最多返回多少条poiitem
		query.setPageNum(0);// 设置查第一页
		query.setLimitDiscount(false);
		query.setLimitGroupbuy(false);
		if (latLonPoint != null) {
			poiSearch = new PoiSearch(this, query);
			poiSearch.setOnPoiSearchListener(this);
			poiSearch.setBound(new SearchBound(latLonPoint, 2000, true));//
			// 设置搜索区域为以lp点为圆心，其周围2000米范围
			/*
			 * List<LatLonPoint> list = new ArrayList<LatLonPoint>();
			 * list.add(lp);
			 * list.add(AMapUtil.convertToLatLonPoint(Constants.BEIJING));
			 * poiSearch.setBound(new SearchBound(list));// 设置多边形poi搜索范围
			 */
			poiSearch.searchPOIAsyn();// 异步搜索
		}

	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getQuery() != null) {// 搜索poi的结果
				if (result.getQuery().equals(query)) {// 是否是同一条
					PoiItem poiItem = null;
					// 取得第一页的poiitem数据，页数从数字0开始
					if (result.getPois() != null && result.getPois().size() > 0) {
						lvList.setAdapter(getMenuAdapter(result.getPois()));
						for (int i = 0; i < result.getPois().size(); i++) {
							poiItem = result.getPois().get(i);
							Log.e("AmapErr",
									"poiItem is  " + poiItem.toString());
						}
					}
				}
			} else {
			}
		}

	}

	private SimpleAdapter getMenuAdapter(ArrayList<PoiItem> itemArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < itemArray.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemText", itemArray.get(i));
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.menu_item, new String[] { "itemText" },
				new int[] { R.id.tvText });
		return simperAdapter;
	}

}
