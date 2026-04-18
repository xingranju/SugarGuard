package com.example.myapplication.ui.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.model.MealType

val portionOptions = listOf(
    "0.25" to "少量",
    "half" to "半份",
    "0.75" to "大半",
    "full" to "整份",
    "1.5"  to "1.5份",
    "2.0"  to "2份"
)

fun portionMultiplier(key: String): Double = when (key) {
    "0.25" -> 0.25
    "half" -> 0.5
    "0.75" -> 0.75
    "1.5"  -> 1.5
    "2.0"  -> 2.0
    else   -> 1.0
}

fun portionDisplayName(key: String): String = when (key) {
    "0.25" -> "少量"
    "half" -> "半份"
    "0.75" -> "大半"
    "full" -> "整份"
    "1.5"  -> "1.5份"
    "2.0"  -> "2份"
    else   -> key
}

/**
 * 添加饮品到日记前的统一选择页（餐次、份量、备注）。
 * [requireExplicitMeal] 为 true 时必须先点选餐次才能保存，避免「默认到某一餐」的误操作。
 * [servingSize] 标准份量(g/ml)，用于自定义分量计算。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDrinkAddBottomSheet(
    drinkName: String,
    sugar: Double,
    calories: Double,
    imageUrl: String?,
    defaultNotes: String,
    requireExplicitMeal: Boolean = true,
    servingSize: Double = 0.0,
    servingSizeUnit: String = "ml",
    onDismiss: () -> Unit,
    onSave: (mealType: String, portionSize: String, notes: String, multiplier: Double) -> Unit
) {
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }
    var selectedPortion by remember { mutableStateOf("full") }
    var notes by remember { mutableStateOf(defaultNotes) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var customMode by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }

    val currentMultiplier = if (customMode) {
        val inputVal = customInput.toDoubleOrNull() ?: 0.0
        if (servingSize > 0 && inputVal > 0) inputVal / servingSize else if (inputVal > 0) inputVal / 100.0 else 1.0
    } else {
        portionMultiplier(selectedPortion)
    }

    val adjustedSugar = sugar * currentMultiplier
    val adjustedCalories = calories * currentMultiplier

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("加入饮食日记", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭", tint = Color(0xFF9CA3AF)) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9FAFB)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(drinkName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Text(
                            "${adjustedSugar.toInt()}g 糖 · ${adjustedCalories.toInt()} kcal",
                            fontSize = 12.sp, color = Color(0xFF9CA3AF)
                        )
                        if (currentMultiplier != 1.0) {
                            Text(
                                "原始: ${sugar.toInt()}g 糖 · ${calories.toInt()} kcal（${portionDisplayName(if (customMode) "自定义" else selectedPortion)}）",
                                fontSize = 10.sp, color = Color(0xFFBDBDBD)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (requireExplicitMeal) "请选择餐次" else "选择餐次",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealType.values().forEach { mt ->
                    val isSelected = selectedMealType == mt
                    Button(
                        onClick = { selectedMealType = mt },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(mt.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("份量", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                portionOptions.forEach { (value, label) ->
                    val isSelected = !customMode && selectedPortion == value
                    Button(
                        onClick = { selectedPortion = value; customMode = false },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { customMode = !customMode },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (customMode) MintGreen else Color(0xFFF9FAFB),
                    contentColor = if (customMode) Color.White else Color(0xFF6B7280)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("自定义分量", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            if (customMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入分量") },
                    suffix = { Text(servingSizeUnit, color = Color(0xFF9CA3AF)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen,
                        cursorColor = MintGreen
                    )
                )
                if (servingSize > 0) {
                    Text(
                        "标准份量: ${servingSize.toInt()}${servingSizeUnit}，含糖 ${sugar.toInt()}g",
                        fontSize = 10.sp, color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                val customVal = customInput.toDoubleOrNull()
                if (customVal != null && customVal > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MintBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("对应糖分", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${adjustedSugar.toInt()}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                            Text("热量", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${adjustedCalories.toInt()}kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注（可选）") },
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            val canSave = (!requireExplicitMeal || selectedMealType != null) &&
                    (!customMode || (customInput.toDoubleOrNull() ?: 0.0) > 0)
            val mealValue = selectedMealType?.value ?: MealType.getCurrentMealType().value
            val portionDesc = if (customMode) {
                "${customInput}${servingSizeUnit}"
            } else {
                portionDisplayName(selectedPortion)
            }

            Button(
                onClick = {
                    onSave(mealValue, portionDesc, notes, currentMultiplier)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(6.dp),
                enabled = canSave
            ) {
                Text("保存到日记", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

/**
 * ViT / 本地识别后的保存页：可选编辑名称（未知食物）、餐次与份量。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitFoodSaveBottomSheet(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    showNameEditor: Boolean,
    sugar: Double,
    calories: Double,
    previewBitmap: Bitmap?,
    previewImageUrl: String?,
    defaultNotes: String,
    servingSize: Double = 0.0,
    servingSizeUnit: String = "g",
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (mealType: String, portionSize: String, notes: String, name: String, multiplier: Double) -> Unit
) {
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }
    var selectedPortion by remember { mutableStateOf("full") }
    var notes by remember { mutableStateOf(defaultNotes) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var customMode by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }

    val currentMultiplier = if (customMode) {
        val inputVal = customInput.toDoubleOrNull() ?: 0.0
        if (servingSize > 0 && inputVal > 0) inputVal / servingSize else if (inputVal > 0) inputVal / 100.0 else 1.0
    } else {
        portionMultiplier(selectedPortion)
    }

    val adjustedSugar = sugar * currentMultiplier
    val adjustedCalories = calories * currentMultiplier

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("确认并加入日记", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭", tint = Color(0xFF9CA3AF)) }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    previewBitmap != null -> {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    !previewImageUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(previewImageUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (showNameEditor) {
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = onFoodNameChange,
                            label = { Text("食物名称") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(foodName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Text(
                        "${adjustedSugar.toInt()}g 糖 · ${adjustedCalories.toInt()} kcal",
                        fontSize = 12.sp, color = Color(0xFF9CA3AF)
                    )
                    if (currentMultiplier != 1.0) {
                        Text(
                            "原始: ${sugar.toInt()}g 糖 · ${calories.toInt()} kcal",
                            fontSize = 10.sp, color = Color(0xFFBDBDBD)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("请选择餐次", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealType.values().forEach { mt ->
                    val isSelected = selectedMealType == mt
                    Button(
                        onClick = { selectedMealType = mt },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(mt.displayName, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("份量", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                portionOptions.forEach { (value, label) ->
                    val isSelected = !customMode && selectedPortion == value
                    Button(
                        onClick = { selectedPortion = value; customMode = false },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { customMode = !customMode },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (customMode) MintGreen else Color(0xFFF9FAFB),
                    contentColor = if (customMode) Color.White else Color(0xFF6B7280)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("自定义分量", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            if (customMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入分量") },
                    suffix = { Text(servingSizeUnit, color = Color(0xFF9CA3AF)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen,
                        cursorColor = MintGreen
                    )
                )
                if (servingSize > 0) {
                    Text(
                        "标准份量: ${servingSize.toInt()}${servingSizeUnit}，含糖 ${sugar.toInt()}g",
                        fontSize = 10.sp, color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                val customVal = customInput.toDoubleOrNull()
                if (customVal != null && customVal > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MintBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("对应糖分", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${adjustedSugar.toInt()}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                            Text("热量", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${adjustedCalories.toInt()}kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注（含 AI 建议，可编辑）") },
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))
            val canSave = selectedMealType != null && foodName.isNotBlank() && !isSaving &&
                    (!customMode || (customInput.toDoubleOrNull() ?: 0.0) > 0)
            val portionDesc = if (customMode) {
                "${customInput}${servingSizeUnit}"
            } else {
                portionDisplayName(selectedPortion)
            }

            Button(
                onClick = {
                    val mt = selectedMealType ?: return@Button
                    onSave(mt.value, portionDesc, notes, foodName.trim(), currentMultiplier)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                enabled = canSave
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("保存到日记", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
