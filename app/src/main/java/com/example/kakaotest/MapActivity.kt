package com.example.kakaotest

import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kakaotest.databinding.ActivityMapBinding
import com.skt.tmap.TMapData
import com.skt.tmap.TMapData.OnFindAllPOIListener
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.TMapView.OnClickListenerCallback
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.poi.TMapPOIItem
import java.util.ArrayList


class MapActivity : AppCompatActivity() {

    val searchDataList = arrayListOf<SearchData>()
    private var mBinding: ActivityMapBinding ?= null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //이부분 tmap sdk에도 BuildConfig가 있어서 고생좀 함
        //여기 오류나면 상단바 Build -> Rebuild Project 누르면 됨
        val appKey: String = BuildConfig.app_key

        // FrameLayout 컨테이너를 XML에서 찾아옴
        val container: FrameLayout = findViewById(R.id.tmapViewContainer)
        // TMapView 인스턴스를 생성
        val tMapView = TMapView(this@MapActivity)
        val tMapData = TMapData()
        val tMapGps = TMapPoint()

        // TMapView를 FrameLayout에 추가
        container.addView(tMapView)
        // 발급받은 키로 TMapView에 API 키 설정
        tMapView.setSKTMapApiKey(appKey)

        val searchDataAdapter = DataAdapter(this,searchDataList)
        binding.searchDataListView.adapter = searchDataAdapter

        binding.searchDataListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectItem = parent.getItemAtPosition(position) as SearchData

            Toast.makeText(this, selectItem.id, Toast.LENGTH_SHORT).show()
            tMapView.setCenterPoint(selectItem.tpoint.latitude,selectItem.tpoint.longitude)
            tMapView.zoomLevel = 15

        }

        // 클릭 이벤트 설정
        tMapView.setOnClickListenerCallback(object : OnClickListenerCallback {
            override fun onPressDown( // 터치함
                p0: ArrayList<TMapMarkerItem>?,
                p1: ArrayList<TMapPOIItem>?,
                p2: TMapPoint?,
                p3: PointF?
            ) {
                Toast.makeText(this@MapActivity, "onPressDown", Toast.LENGTH_SHORT).show()
            }

            override fun onPressUp( // 떨어짐
                p0: ArrayList<TMapMarkerItem>?,
                p1: ArrayList<TMapPOIItem>?,
                p2: TMapPoint?,
                p3: PointF?
            ) {
                Toast.makeText(this@MapActivity, "onPressUp", Toast.LENGTH_SHORT).show()
            }
        })





        // 맵 로딩 완료 시 동작할 리스너 설정
        tMapView.setOnMapReadyListener(object : TMapView.OnMapReadyListener {
            override fun onMapReady() {
                // 맵 로딩이 완료된 후에 수행할 동작을 구현해주세요
                // 예: 마커 추가, 경로 표시 등
                Toast.makeText(this@MapActivity, "MapLoading", Toast.LENGTH_SHORT).show()
                tMapView.setCenterPoint(tMapGps.katecLat, tMapGps.katecLon)
                tMapView.zoomLevel = 10

                val marker = TMapMarkerItem()
                marker.id = "marker1"
                marker.setTMapPoint(tMapGps.katecLat, tMapGps.katecLon)
                marker.icon = BitmapFactory.decodeResource(resources, R.drawable.point)
                tMapView.addTMapMarkerItem(marker)


                binding.searchButton.setOnClickListener{
                    tMapView.removeAllTMapMarkerItem()
                    tMapView.removeAllTMapPOIItem()
                    searchDataList.clear()
                    var strData = binding.searchText.text.toString()
                    tMapData.findAllPOI(strData,
                        OnFindAllPOIListener { poiItemList ->
                            var num = 1
                            for (item in poiItemList) {
                                Log.e(
                                    "Poi Item",
                                    "name:" + item.poiName + " address:" + item.poiAddress
                                )
                                marker.id = item.poiName
                                marker.setTMapPoint(TMapPoint())
                                marker.icon = BitmapFactory.decodeResource(resources, R.drawable.poi)
                                tMapView.addTMapMarkerItem(marker)
                                runOnUiThread{
                                    searchDataList.add(SearchData(item.poiName,item.poiPoint,item.poiAddress))
                                    searchDataAdapter.notifyDataSetChanged()
                                }
                                num += 1
                            }
                            tMapView.addTMapPOIItem(poiItemList)
                        })
                }
            }
        })


    }

}