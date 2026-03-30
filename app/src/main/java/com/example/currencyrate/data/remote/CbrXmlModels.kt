package com.example.currencyrate.data.remote

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
data class ValCurs(
    @field:ElementList(inline = true, entry = "Valute", required = false)
    var valutes: List<Valute>? = null,
    
    @field:ElementList(inline = true, entry = "Record", required = false)
    var records: List<ValRecord>? = null
)

@Root(name = "Valute", strict = false)
data class Valute(
    @field:Attribute(name = "ID", required = false)
    var id: String = "",
    @field:Element(name = "CharCode")
    var code: String = "",
    @field:Element(name = "Name")
    var name: String = "",
    @field:Element(name = "Value")
    var value: String = "",
    @field:Element(name = "Nominal")
    var nominal: Int = 1
)

@Root(name = "Record", strict = false)
data class ValRecord(
    @field:Attribute(name = "Date")
    var date: String = "",
    @field:Element(name = "Value")
    var value: String = "",
    @field:Element(name = "Nominal")
    var nominal: Int = 1
)
