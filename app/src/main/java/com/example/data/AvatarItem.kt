package com.example.data

data class AvatarItem(
    val id: String,
    val name: String,
    val category: String, // "hat", "face", "shirt", "pants"
    val price: Int, // Robux cost
    val colorHex: String, // Hex color for canvas drawings
    val unlockedByDefault: Boolean = false,
    val description: String = ""
) {
    companion object {
        val ALL_ITEMS = listOf(
            // Hats
            AvatarItem("none", "No Hat", "hat", 0, "#00000000", true, "Enjoy the breeze!"),
            AvatarItem("fedora", "Classic Fedora", "hat", 30, "#3E2723", false, "Dapper and mysterious."),
            AvatarItem("valkyrie", "Golden Valkyrie", "hat", 120, "#FFD700", false, "The crown of champions."),
            AvatarItem("crown", "Royal Crown", "hat", 80, "#FF8F00", false, "A heavy crown for a wealthy bloxer."),
            AvatarItem("cap", "Red Baseball Cap", "hat", 15, "#D84315", false, "Casual and ready for action."),

            // Faces
            AvatarItem("smile", "Classic Smile", "face", 0, "#000000", true, "The happy face we all know and love."),
            AvatarItem("chilled", "Chilled Shades", "face", 20, "#1976D2", false, "Cool sunglasses and a relaxed grin."),
            AvatarItem("beast", "Beast Mode", "face", 90, "#C62828", false, "Unleash the gaming beast!"),
            AvatarItem("winning", "Winning Smile", "face", 10, "#4E342E", false, "A confident smile to show off your wins."),

            // Shirts
            AvatarItem("admin_hoodie", "Admin Hoodie", "shirt", 0, "#212121", true, "Exclusive admin-styled hoodie."),
            AvatarItem("galaxy", "Galaxy T-Shirt", "shirt", 25, "#6A1B9A", false, "A cosmic shirt printed with stars."),
            AvatarItem("tuxedo", "Sharp Tuxedo", "shirt", 50, "#37474F", false, "Looking extremely polished."),
            AvatarItem("bloxsweater", "Blox Sweater", "shirt", 15, "#AD1457", false, "Warm knitted winter sweater."),

            // Pants
            AvatarItem("jeans", "Blue Jeans", "pants", 0, "#1565C0", true, "Reliable denim pants."),
            AvatarItem("goldpants", "Solid Gold Pants", "pants", 100, "#FBC02D", false, "Literally made of solid gold!"),
            AvatarItem("shorts", "Active Shorts", "pants", 10, "#2E7D32", false, "Perfect for athletic speedruns.")
        )

        fun getItemById(id: String): AvatarItem? {
            return ALL_ITEMS.find { it.id == id }
        }
    }
}
