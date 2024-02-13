package com.gnbsoftec.dolphinnative.web.model

object InterfaceModel {
    interface BaseInput {
        val cmd: String
        val callback: String
    }
    interface BaseOutput {
        val resultCd: String
        val resultMsg: String
    }

    /**
     * [1] : 기기정보 조회
     */
    data class InGetDeviceInfo(
        override val cmd: String,
        override val callback: String
    ):BaseInput
    data class OutGetDeviceInfo(
        override val resultCd: String,
        override val resultMsg: String,
        val deviceInfo: Map<String,String>
    ):BaseOutput
    /**
     * [2] : 로딩 호출
     */
    data class InLoading(
        override val cmd: String,
        override val callback: String,
        val flag: String
    ):BaseInput
    data class OutLoading(
        override val resultCd: String,
        override val resultMsg: String
    ):BaseOutput
    /**
     * [3] : 내부저장소 데이터 적재
     */
    data class InSetLocalStorage(
        override val cmd: String,
        override val callback: String,
        val data: Map<String,String>
    ):BaseInput
    data class OutSetLocalStorage(
        override val resultCd: String,
        override val resultMsg: String
    ):BaseOutput

    /**
     * [4] : 내부저장소 데이터 조회
     */
    data class InGetLocalStorage(
        override val cmd: String,
        override val callback: String,
        val keys: List<String>
    ):BaseInput
    data class OutGetLocalStorage(
        override val resultCd: String,
        override val resultMsg: String,
        val value: Map<String,String>
    ):BaseOutput

    /**
     * [5] : 갤러리 이미지 조회
     */
    data class InGetGalleryImage(
        override val cmd: String,
        override val callback: String
    ):BaseInput
    data class OutGetGalleryImage(
        override val resultCd: String,
        override val resultMsg: String,
        val images: List<Map<String,String>>
    ):BaseOutput
    /**
     * [6] : 일반카메라 조회
     */
    data class InGetCameraImage(
        override val cmd: String,
        override val callback: String
    ):BaseInput
    data class OutGetCameraImage(
        override val resultCd: String,
        override val resultMsg: String,
        val images: List<Map<String,String>>
    ):BaseOutput

    /**
     * [7] : 파일다운로드 url
     */
    data class InUrlFileDownload(
        override val cmd: String,
        override val callback: String,
        val fileUrl: String
    ):BaseInput
    data class OutUrlFileDownload(
        override val resultCd: String,
        override val resultMsg: String
    ):BaseOutput
    /**
     * [8] : 파일다운로드 base64
     */
    data class InBase64FileDownload(
        override val cmd: String,
        override val callback: String,
        val fileName: String,
        val base64str: String
    ):BaseInput
    data class OutBase64FileDownload(
        override val resultCd: String,
        override val resultMsg: String
    ):BaseOutput
    /**
     * [9] : 퍼미션 상태 조회
     */
    data class InPermissionSelect(
        override val cmd: String,
        override val callback: String
    ):BaseInput
    data class OutPermissionSelect(
        override val resultCd: String,
        override val resultMsg: String,
        val chkeckYn: String
    ):BaseOutput
    /**
     * [10] : 퍼미션 체크
     */
    data class InPermissionCheck(
        override val cmd: String,
        override val callback: String
    ):BaseInput
    data class OutPermissionCheck(
        override val resultCd: String,
        override val resultMsg: String,
        val chkeckYn: String
    ):BaseOutput
    /**
     * [11] : 토스트 메세지 출력
     */
    data class InShowToast(
        override val cmd: String,
        override val callback: String,
        val msg: String
    ):BaseInput
    data class OutShowToast(
        override val resultCd: String,
        override val resultMsg: String
    ):BaseOutput
    /**
     * [12] : 알림 메세지 출력
     */
    data class InShowNotification(
        override val cmd: String,
        override val callback: String,
        val title: String,
        val msg: String
    ):BaseInput
    data class OutShowNotification(
        override val resultCd: String,
        override val resultMsg: String
    ):BaseOutput


    /**
     * [13] : 푸쉬 리스트
     */
    data class InPushList(
        override val cmd: String,
        override val callback: String,
        val title: String,
        val msg: String
    ):BaseInput
    data class OutPushList(
        override val resultCd: String,
        override val resultMsg: String,
        val list: List<Map<String,String>>
    ):BaseOutput

    /**
     * [14] : 푸쉬 전체 삭제
     */
    data class InPushDelete(
        override val cmd: String,
        override val callback: String
    ):BaseInput
    data class OutPushDelete(
        override val resultCd: String,
        override val resultMsg: String,
        val cnt: Int
    ):BaseOutput

    /**
     * [15] : 푸쉬 구독 명 변경
     */
    data class InPushTopicUpdate(
        override val cmd: String,
        override val callback: String,
        val topic:String
    ):BaseInput
    data class OutPushTopicUpdate(
        override val resultCd: String,
        override val resultMsg: String,
        val topicYn:String,
        val topicMsg:String
    ):BaseOutput
}