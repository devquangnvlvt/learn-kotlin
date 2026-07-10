package com.example.kotlinv4.ui.utils

object KeyApp {
    const val BASE_URL            = "https://lvtglobal.site/"
    const val BASE_URL_PREVENTIVE = "https://lvt-api-site.io.vn/"
    const val SUB_DOMAIN          = "public/app/ST185_CharacterCreatorPFPMaker"
    const val API_KEY             = "api/app/ST185_CharacterCreatorPFPMaker"


    // lây avatar
    // Ảnh đại diện cho cả nhóm (dùng ở màn CategoryActivity, lưới chọn nhóm)
    fun getAvatarUrl(groupKey: String): String =
        "$BASE_URL$SUB_DOMAIN/$groupKey/avatar.png"

    // Ảnh nav (thumbnail) để chọn item trong 1 nhóm — dùng chung cho TabAdapter và OptionAdapter
    // Ví dụ: https://lvtglobal.site/public/app/ST185_CharacterCreatorPFPMaker/data1/6-1/nav.webp
    fun getNavUrl(groupKey: String, parts: String): String =
        "$BASE_URL$SUB_DOMAIN/$groupKey/$parts/nav.webp"

    // Ảnh layer thật để ghép lên nhân vật.
    // imageIndex: số random từ 1..quantity (đã chốt: random, không cho chọn).
    // color: mã hex nếu item có colorArray, null nếu không đổi màu.
    // Ví dụ có màu: .../data1/8-3/302d2d/1.webp
    // Ví dụ không màu (giả định, cần bạn xác nhận 1 URL thật): .../data1/6-1/1.webp
    fun getLayerUrl(groupKey: String, parts: String, imageIndex: Int, color: String? = null): String {
        return if (!color.isNullOrEmpty()) {
            "$BASE_URL$SUB_DOMAIN/$groupKey/$parts/$color/$imageIndex.webp"
        } else {
            "$BASE_URL$SUB_DOMAIN/$groupKey/$parts/$imageIndex.webp"
        }
    }

}