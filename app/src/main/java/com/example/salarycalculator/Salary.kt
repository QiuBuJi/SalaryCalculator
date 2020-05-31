package com.example.salarycalculator

data class GroupHours(
    var hours: Float = 0f,
    var hoursExtra: Float = 0f,
    var hoursWeekend: Float = 0f
)

class Salary {
    constructor()
    constructor(
        hourNormal: Float,
        hourExtra: Float,
        hourWeekend: Float
    ) {
        this.hourNormal = hourNormal
        this.hourExtra = hourExtra
        this.hourWeekend = hourWeekend
    }

    constructor(groupHours: GroupHours) {
        this.hourNormal = groupHours.hours
        this.hourExtra = groupHours.hoursExtra
        this.hourWeekend = groupHours.hoursWeekend
    }

    constructor(
        hourNormal: Float,
        hourExtra: Float,
        hourWeekend: Float,
        cutPayment: Float = 0f,
        isNightDays: Boolean = false
    ) {
        this.hourNormal = hourNormal
        this.hourExtra = hourExtra
        this.hourWeekend = hourWeekend
        this.isNightDays = isNightDays
    }

    /**平时上班小时*/
    var hourNormal: Float = 0f

    /**平时加班小时*/
    var hourExtra: Float = 0f

    /**周末加班小时*/
    var hourWeekend: Float = 0f

    /**扣款*/
    var cutPayment: Float = 0f

    /**上班天数*/
    var days: Int = 0

    /**夜班补贴*/
    val salaryNightExtra: Float
        get() = if (isNightDays) days * 20f else 0f

    /**上班天数*/
    val daysNormal: Float
        get() = hourNormal / 8

    /**绩效奖*/
    val salaryBonus: Float
        get() = 600 / daysMax * daysNormal

    /**平时时薪*/
    val hourSalary: Float
        get() = salaryBasic / daysMax / 8f

    /**餐补*/
    val salaryFood: Float
        get() = 180 / daysMax * daysNormal

    /**平时薪资*/
    val salaryNormal: Float
        get() = hourNormal * hourSalary

    /**平时加班薪资*/
    val salaryExtra: Float
        get() = hourExtra * hourSalary * 1.5f

    /**周末薪资*/
    val salaryWeekend: Float
        get() = hourWeekend * hourSalary * 2

    /**全勤奖*/
    val salaryFull: Float
        get() = if (daysMax <= daysNormal) 100f else 0f

    /**实际工资*/
    val salaryTotal: Float
        get() = salaryNormal + salaryExtra + salaryWeekend + salaryBonus + salaryFull + salaryNightExtra

    /**基本工资*/
    var salaryBasic = 1700f

    /**最大出勤天数*/
    var daysMax = 22f

    /**是否为夜班*/
    var isNightDays = false

    /**显示工资组成*/
    override fun toString(): String {
        return "平时加班：${salaryExtra.two()}元\n" +
                "周末加班：${salaryWeekend.two()}元\n" +
                "平时工资：${salaryNormal.two()}元\n" +
                "加班工资：${(salaryNormal + salaryWeekend).two()}元\n" +
                "夜班补贴：${salaryNightExtra.two()}元\n" +
                "绩效奖金：${salaryBonus.two()}元\n" +
                "工资扣款：${cutPayment.two()}元\n" +
                "全勤奖金：${salaryFull.two()}元\n" +
                "应发工资：${salaryTotal.two()}元\n" +
                "实发工资：${(salaryTotal - cutPayment).two()}元\n\n" +
                "伙食补贴：${salaryFood.two()}元\n"
    }
}

/**只显示小数点后2位*/
fun Float.two(): String {
    return String.format("%.2f", this)
}