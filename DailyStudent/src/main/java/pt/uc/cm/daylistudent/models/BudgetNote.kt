package pt.uc.cm.daylistudent.models

class BudgetNote {
    var id: Int = 0
        private set
    var title: String? = null
        private set
    var gender: String = ""
    var value: Double? = null
        private set
    var date: String = ""

    constructor(id: Int, title: String, value: Double?) {
        this.id = id
        this.title = title
        this.value = value
    }

    constructor(id: Int, title: String, gender: String, value: Double?, date: String) {
        this.id = id
        this.gender = gender
        this.title = title
        this.value = value
        this.date = date
    }
}
