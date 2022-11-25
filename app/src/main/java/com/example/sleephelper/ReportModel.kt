package com.example.sleephelper

object ReportModel {
    fun defaultReportDataList():ArrayList<ReportData>{
        val reportDataList = ArrayList<ReportData>()


        val score = ReportData("85","79")
        reportDataList.add(score)

        val timeInBed = ReportData("8:15","8:04")
        reportDataList.add(timeInBed)

        val sleepTime = ReportData("7:42", "7:50")
        reportDataList.add(sleepTime)

        val BI = ReportData("46","32")
        reportDataList.add(BI)

        val bedTime = ReportData("23:05", "23:12")
        reportDataList.add(bedTime)

        val wakeUpTime = ReportData("07:00", "07:00")
        reportDataList.add(wakeUpTime)

        val napTime = ReportData("00:35","00:12")
        reportDataList.add(napTime)

        val coffee = ReportData("3잔","2.5잔")
        reportDataList.add(coffee)

        val beer = ReportData("3잔","2.5잔")
        reportDataList.add(beer)

        val soju = ReportData("3잔","2.5잔")
        reportDataList.add(soju)

        val makgurli = ReportData("3잔","2.5잔")
        reportDataList.add(makgurli)

        val wine = ReportData("3잔","2.5잔")
        reportDataList.add(wine)

        val date = "20221124"

        return reportDataList
    }
}