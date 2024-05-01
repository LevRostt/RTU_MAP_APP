package ru.levrost.rtu_map_app.global

interface ResultStatus {
    object Success : ResultStatus
    object UserFail : ResultStatus
    object ServerFail : ResultStatus
}