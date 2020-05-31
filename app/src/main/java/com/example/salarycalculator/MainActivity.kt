package com.example.salarycalculator

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

typealias ItemList = LinkedList<MainActivity.Item>


//给Calendar扩展一些简便操作的变量
val Calendar.year: Int
    get() = get(Calendar.YEAR)
val Calendar.month: Int
    get() = get(Calendar.MONTH) + 1
val Calendar.day: Int
    get() = get(Calendar.DAY_OF_MONTH)
val Calendar.dayOfWeek: Int
    get() = get(Calendar.DAY_OF_WEEK)


//主类
class MainActivity : AppCompatActivity() {
    private val externalDir: File = Environment.getExternalStorageDirectory()
    private val file = File(externalDir, "SalaryCalculation.txt")

    var data = ItemList()
    private val dataSet = LinkedHashMap<String, ItemList>()

    private lateinit var salary: Salary
    private var isDataChange = false
    private lateinit var sp: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.run {
            add(0, 1, 1, "关于")
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.run {
            startActivity(Intent(this@MainActivity, About::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sp = getSharedPreferences("SalaryCalculator", Context.MODE_PRIVATE)
        editor = sp.edit()

        try {
            val strLines = FileReader(file).readLines()

            //读入数据
            for (str in strLines) {
                if (str.matches(Regex("\\d+\\.\\d+"))) {
                    data = ItemList()
                    dataSet[str] = data

                } else {

                    if (str.isBlank()) continue
                    val part = str.split(" ")

                    data.add(
                        Item(
                            part[0].toInt(),
                            part[1].toFloat(),
                            part[2].toFloat(),
                            part[3].toInt(),
                            if (part.size > 4) part[4] else ""
                        )
                    )
                }

            }

        } catch (e: Exception) {
        }

        val strYearMonth = sp.getString("year_month", "")
        if (main_sw.isChecked xor sp.getBoolean("isChecked", false)) main_sw.toggle()
        main_tvMonth.text = strYearMonth

        //设置要显示的数据
        data =
            dataSet[strYearMonth] ?: try {
                val first = dataSet.entries.first()
                main_tvMonth.text = first.key
                editor.putString("year_month", first.key).apply()
                first.value
            } catch (e: Exception) {
                main_tvMonth.text = ""
                ItemList()
            }


        val adapter = Adapter()
        main_rvList.adapter = adapter
        main_rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        //设置选择的时间
        val ins = Calendar.getInstance()
        main_tvDate.text = "${ins.year}.${ins.month}.${ins.day}"

        //设置选择的倍数
        main_spTimes.setSelection(
            when (ins.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY, Calendar.SUNDAY -> 1
                else -> 0
            }
        )

        //时间选择，单击
        val spTimes = main_spTimes
        main_tvDate.setOnClickListener {
            val split = main_tvDate.text.split(".")
            val nYear = split[0].toInt()
            val nMonth = split[1].toInt()
            val nDay = split[2].toInt()

            DatePickerDialog(this, null, nYear, nMonth - 1, nDay).run {
                setOnDateSetListener { _, year, month, dayOfMonth ->
                    (it as TextView).text = "$year.${month + 1}.$dayOfMonth"

                    val ins = Calendar.getInstance().apply { set(year, month, dayOfMonth) }

                    spTimes.setSelection(
                        when (ins.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.SATURDAY, Calendar.SUNDAY -> 1
                            else -> 0
                        }
                    )

                }
                setCanceledOnTouchOutside(true)
                show()
            }
        }

        //设置时间到今日
        main_tvDate.setOnLongClickListener {
            val ins = Calendar.getInstance()

            //设置选择的时间
            main_tvDate.text =
                "${ins.get(Calendar.YEAR)}.${ins.get(Calendar.MONTH) + 1}.${ins.get(Calendar.DAY_OF_MONTH)}"

            return@setOnLongClickListener true
        }

        //平时加班，长按
        main_etHourExtra.setOnLongClickListener {
            val list = arrayListOf(0.0f, 1.0f, 1.5f, 2.0f, 2.5f)

            data.forEach { list.add(it.hourExtra) }
            val hashSet = HashSet(list)
            list.clear()
            list.addAll(hashSet)
            list.sort()

            ListPopupWindow(this).run {
                setAdapter(
                    ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        list
                    )
                )
                anchorView = main_etHourExtra
                isModal = true
                width = 200
                setOnItemClickListener { _, _, position, _ ->
                    main_etHourExtra.setText(list[position].toString())
                    dismiss()
                }
                show()
            }
            return@setOnLongClickListener true
        }

        //平时小时，长按
        main_etHour.setOnLongClickListener {
            val list = arrayListOf(0, 8, 9, 10, 10.5)

            ListPopupWindow(this).run {
                setAdapter(
                    ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        list
                    )
                )
                anchorView = main_etHour
                isModal = true
                width = 200
                setOnItemClickListener { _, _, position, _ ->
                    main_etHour.setText(list[position].toString())
                    dismiss()
                }
                show()
            }
            return@setOnLongClickListener true
        }

        //基本工资，长按
        main_etBasicSalary.setOnLongClickListener {
            val list = arrayListOf(1600, 1700, 1800, 1900)

            ListPopupWindow(this).run {
                setAdapter(
                    ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        list
                    )
                )
                anchorView = main_etBasicSalary
                isModal = true
                width = 250
                setOnItemClickListener { _, _, position, _ ->
                    main_etBasicSalary.setText(list[position].toString())
                    dismiss()
                }
                show()
            }
            return@setOnLongClickListener true
        }

        //提交数据
        main_btSubmit.setOnClickListener {
            val split = main_tvDate.text.split(".")
            val nDay = split[2].toInt()
            val strRegion = "${split[0]}.${split[1]}"
            val tempData = dataSet[strRegion] ?: ItemList().apply { dataSet[strRegion] = this }

            if (tempData != data) {
                data = tempData
                main_tvMonth.text = strRegion
                adapter.notifyDataSetChanged()
                editor.putString("year_month", strRegion).apply()
            }

            var strText = main_etHour.text.toString()
            if (strText.isEmpty()) strText = "0"
            val nHour = strText.toFloat()

            strText = main_etHourExtra.text.toString()
            if (strText.isEmpty()) strText = "0"
            var nHourExtra = strText.toFloat()

            val strTimes = main_spTimes.selectedItem as String
            val times = strTimes.removeSuffix("倍").toInt()

            if (times > 1) nHourExtra = 0f
            val item = Item(nDay, nHour, nHourExtra, times, main_etNote.text.toString())

            //添加数据到中间
            tempData.forEachIndexed { index, itemCurr ->
                when {
                    itemCurr.day == nDay -> {
                        AlertDialog.Builder(this)
                            .setTitle("数据已经存在，是否替换？")
                            .setPositiveButton("替换") { _, _ ->
                                itemCurr.hour = item.hour
                                itemCurr.hourExtra = item.hourExtra
                                itemCurr.times = item.times
                                itemCurr.note = item.note

                                adapter.notifyItemChanged(index)
                                dataChanged()
                            }
                            .setNegativeButton("取消") { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }
                    itemCurr.day > nDay -> {
                        adapter.notifyItemInserted(index)
                        adapter.notifyItemRangeChanged(index + 1, tempData.size)
                        tempData.add(index, item)
                        dataChanged()
                        return@setOnClickListener
                    }
                }
            }

            //添加数据到末尾
            tempData.add(item)
            adapter.notifyItemInserted(tempData.size)
            dataChanged()
            main_rvList.scrollToPosition(tempData.size - 1)

            isDataChange = true
        }

        showSalary()

        //滚动到底部
        main_rvList.scrollToPosition(data.size - 1)

        //白班、夜班选择按钮，监听器
        main_sw.setOnCheckedChangeListener { _, isChecked ->
            dataChanged()
            editor.putBoolean("isChecked", isChecked).apply()
        }

        //最大天数改变，监听器
        main_spMaxDay.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) = dataChanged()
            }

        //倍数改变，监听器
        main_spTimes.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val strTimes = main_spTimes.selectedItem as String
                    when {
                        strTimes.contains("1") -> {
                            main_etHourExtra.isEnabled = true
                        }
                        else -> {
                            main_etHourExtra.isEnabled = false
                            main_etHourExtra.setText("")
                        }
                    }
                }

            }

        //字符串改变，监听器
        val value = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = dataChanged()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
                Unit
        }
        main_etBasicSalary.addTextChangedListener(value)
        main_etCutpayment.addTextChangedListener(value)

        //年月时间改变，监听器
        main_tvMonth.setOnClickListener {
            val set = ArrayList<String>()
            dataSet.forEach { set.add(it.key) }
            set.sortBy { it.toFloat() }

            ListPopupWindow(this).run {
                val resource = android.R.layout.simple_list_item_1
                setAdapter(ArrayAdapter(this@MainActivity, resource, set))
                isModal = true
                anchorView = main_tvMonth
                width = 260
                setOnItemClickListener { _, _, position, _ ->
                    val strTemp = set[position]
                    main_tvMonth.text = strTemp

                    editor.putString("year_month", strTemp).apply()
                    dataSet[strTemp]?.run { data = this }
                    adapter.notifyDataSetChanged()
                    dismiss()
                    dataChanged()
                }
                show()
            }
        }

        //显示各种上班小时，单击，监听器
        main_tvTitleLeft.setOnClickListener {
            Toast.makeText(
                this,
                "平时上班：${salary.hourNormal}h\n平时加班：${salary.hourExtra}h\n周末加班：${salary.hourWeekend.two()}h\n上班天数：${salary.days}天",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val ins = Calendar.getInstance()

        val strDayOfWeek = when (ins.dayOfWeek) {
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            Calendar.SUNDAY -> "周日"
            else -> "周错误"
        }

        val size = 18
        val section = "今天："
        val section1 = "${ins.day}号 $strDayOfWeek"
        SpanUtil.create()
            .addAbsSizeSection(section, size).setForeColor(section, Color.GRAY)
            .addAbsSizeSection(section1, (size * 1.8).toInt())
            .setForeColor(section1, Color.BLACK)
            .showIn(main_tvCurrDate)


        //设置选择的时间
        main_tvDate.text = "${ins.year}.${ins.month}.${ins.day}"
    }

    override fun onStop() {
        super.onStop()

        if (!isDataChange) return

        if (!file.exists()) file.createNewFile()
        val fileWriter = FileWriter(file)
        val sb = StringBuilder()

        for (entry in dataSet) {
            sb.append("${entry.key}\n")
            for (datum in entry.value) {
                sb.append("${datum.day} ${datum.hour} ${datum.hourExtra} ${datum.times} ${datum.note}\n")
            }
        }

        fileWriter.write(sb.toString())
        fileWriter.flush()
        fileWriter.close()
        Toast.makeText(this, "数据保存成功！", Toast.LENGTH_SHORT).show()
    }


    private fun LinkedList<Item>.getGroupHours(): GroupHours {
        val group = GroupHours()

        data.forEach {
            if (it.times > 1) {
                it.hourExtra = 0f
                group.hoursWeekend += it.hour/* * it.times*/

            } else group.hours += it.hour

            group.hoursExtra += it.hourExtra
        }
        return group
    }


    private fun showSalary() {
        salary = Salary(data.getGroupHours())

        //是否夜班
        salary.isNightDays = main_sw.isChecked
        //扣款
        var strValue = main_etCutpayment.text.toString()

        salary.cutPayment = if (strValue.isNotEmpty()) strValue.toFloat() else 0f
        salary.days = data.size
        salary.daysMax = (main_spMaxDay.selectedItem as String).toFloat()

        strValue = main_etBasicSalary.text.toString()
        salary.salaryBasic = if (strValue.isNotEmpty()) strValue.toFloat() else 0f


        val sizeTitle = 26
        val sizeValue = (sizeTitle * 1.7).toInt()
        val color = Color.GRAY
        val strSalaryWeekend = "周末加班："
        val strSalaryExtra = "加班工资："
        val section1 = "夜班补贴："
        val section2 = "应发工资："
        var total = (salary.salaryExtra + salary.salaryWeekend).two()

        SpanUtil.create()
            .addAbsSizeSection("平时加班：", sizeTitle).setForeColor(color)
            .addAbsSizeSection("${salary.salaryExtra.two()}元\n", sizeValue)
            .addAbsSizeSection(strSalaryWeekend, sizeTitle)
            .setForeColor(strSalaryWeekend, color)
            .addAbsSizeSection("${salary.salaryWeekend.two()}元\n", sizeValue)
            .addAbsSizeSection(strSalaryExtra, sizeTitle).setForeColor(strSalaryExtra, color)
            .addAbsSizeSection("${total}元\n", sizeValue)
            .addAbsSizeSection(section1, sizeTitle).setForeColor(section1, color)
            .addAbsSizeSection("${salary.salaryNightExtra.two()}元\n", sizeValue)
            .addAbsSizeSection(section2, sizeTitle).setForeColor(section2, color)
            .addAbsSizeSection("${salary.salaryTotal.two()}元\n", sizeValue)
            .showIn(main_tvTitleLeft)


        val section = "${salary.salaryFood.two()}元"
        val section3 = "绩效奖金："
        val section10 = "平时工资："
        val section4 = "工资扣款："
        val section5 = "全勤奖金："
        val section6 = "伙食补贴："
        val section7 = "实发工资："
        val section8 = "（不计数）\n"
        total = (salary.salaryTotal - salary.cutPayment).two()
        val section9 = "${total}元"

        SpanUtil.create()
            .addAbsSizeSection(section10, sizeTitle).setForeColor(section10, color)
            .addAbsSizeSection("${salary.salaryNormal.two()}元\n", sizeValue)
            .addAbsSizeSection(section3, sizeTitle).setForeColor(section3, color)
            .addAbsSizeSection("${salary.salaryBonus.two()}元\n", sizeValue)
            .addAbsSizeSection(section4, sizeTitle).setForeColor(section4, color)
            .addAbsSizeSection("${salary.cutPayment.two()}元\n", sizeValue)
            .addAbsSizeSection(section5, sizeTitle).setForeColor(section5, color)
            .addAbsSizeSection("${salary.salaryFull.two()}元\n", sizeValue)
            .addAbsSizeSection(section6, sizeTitle).setForeColor(section6, color)
            .addAbsSizeSection(section, sizeValue).setForeColor(section, color)
            .addAbsSizeSection(section8, sizeTitle).setForeColor(section8, color)
            .addAbsSizeSection(section7, sizeTitle).setForeColor(section7, color)
            .addAbsSizeSection(section9, sizeValue).setForeColor(section9, Color.BLUE)
            .setStyle(section9, 1)
            .showIn(main_tvTitleRight)
    }


    fun Int.both(): String = if (this > 9) "$this" else "0$this"


    fun Float.single(): String {
        val toInt = this.toInt()
        val point = this - toInt
        return if (point > 0.0) "$this" else "$toInt"
    }


    fun dataChanged() {
        showSalary()
    }


    inner class Adapter : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.list_item, parent, false)
            return Holder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.run {
                tvIndex.text = "${(position + 1).both()}"

                val item = data[position]

                tvDay.text = "${item.day.both()}日"
                tvHour.text = "${item.hour.single()}"
                tvHourExtra.text = "${item.hourExtra.single()}"
                tvTimes.text = "${item.times}倍"


                val ins = Calendar.getInstance()
                val nDay = ins.day - item.day

                val strNear = when {
                    nDay == 2 -> "前天"
                    nDay == 1 -> "昨天"
                    nDay == 0 -> "今天"
                    nDay == -1 -> "明天"
                    nDay == -2 -> "后天"
                    nDay <= 7 -> {
                        ins.set(Calendar.DAY_OF_MONTH, item.day)

                        when (ins.dayOfWeek) {
                            Calendar.MONDAY -> "周一"
                            Calendar.TUESDAY -> "周二"
                            Calendar.WEDNESDAY -> "周三"
                            Calendar.THURSDAY -> "周四"
                            Calendar.FRIDAY -> "周五"
                            Calendar.SATURDAY -> "周六"
                            Calendar.SUNDAY -> "周日"
                            else -> "周错误"
                        }
                    }
                    nDay > 7 -> "${(nDay / 7f).toInt()}周前"
                    nDay < 7 -> "1周后"
                    else -> ""
                }

                val daySalary =
                    ((item.hour * item.times * salary.hourSalary) + (item.hourExtra * salary.hourSalary * 1.5f)).two()

                val size = 28
                val section = "${daySalary}元    "
                SpanUtil.create()
                    .addAbsSizeSection("收入：", size).setForeColor(Color.GRAY)
                    .addAbsSizeSection(section, size).setForeColor(section, Color.BLACK)
                    .addAbsSizeSection(strNear, size)
                    .showIn(tvNear)

                if (item.times > 1) {
                    tvTimes.visibility = View.VISIBLE
                    tvHourExtra.visibility = View.GONE
                } else {
                    tvTimes.visibility = View.INVISIBLE
                    tvHourExtra.visibility = View.VISIBLE
                }
                tvNote.text = item.note
                tvHourExtra.visibility = if (tvHourExtra.text == "0") View.GONE else View.VISIBLE

                //删除数据
                itemView.setOnLongClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("确定删除数据？")
                        .setPositiveButton("删除") { _, _ ->
                            val index = data.indexOf(item)
                            data.removeAt(index)
                            notifyItemRemoved(index)
                            dataChanged()

                            if (data.isEmpty()) {
                                for (entry in dataSet) {
                                    if (entry.value == data) dataSet.remove(entry.key)
                                }
                                //设置要显示的数据
                                data =
                                    try {
                                        val first = dataSet.entries.first()
                                        main_tvMonth.text = first.key
                                        first.value
                                    } catch (e: Exception) {
                                        ItemList()
                                    }
                            }
                            isDataChange = true
                        }
                        .setNegativeButton("取消") { _, _ -> }
                        .show()
                    return@setOnLongClickListener true
                }

                itemView.setOnClickListener {
                    val text = main_tvDate.text
                    val removeRange = text.removeRange(text.lastIndexOf("."), text.length)
                    main_tvDate.text = "$removeRange.${item.day}"
                    main_etHour.setText("${item.hour}")
                    main_etHourExtra.setText("${item.hourExtra}")
                    main_spTimes.setSelection(item.times - 1)
                    main_etNote.setText(item.note)
                }
            }
        }

    }


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIndex: TextView = itemView.findViewById(R.id.listItem_tvIndex)
        val tvDay: TextView = itemView.findViewById(R.id.listItem_tvDay)
        val tvHour: TextView = itemView.findViewById(R.id.listItem_tvHour)
        val tvHourExtra: TextView = itemView.findViewById(R.id.listItem_tvHourExtra)
        val tvTimes: TextView = itemView.findViewById(R.id.listItem_tvDouble)
        val tvNote: TextView = itemView.findViewById(R.id.listItem_tvNote)
        val tvNear: TextView = itemView.findViewById(R.id.listItem_tvNear)
    }


    data class Item(
        var day: Int,
        var hour: Float,
        var hourExtra: Float = 0f,
        var times: Int = 1,
        var note: String = ""
    )
}
