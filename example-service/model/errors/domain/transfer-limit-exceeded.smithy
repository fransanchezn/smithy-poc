$version: "2"

namespace com.example

// ------------ TransferLimitExceeded Domain Error ------------
structure TransferLimitExceededAttributes {
    @memberExample(15000.00)
    @required
    amount: BigDecimal

    @memberExample("USD")
    @required
    currency: String
}

@error("client")
@httpError(422)
structure TransferLimitExceededDomainApiErrorException with [DomainApiErrorException] {
    @const("Transfer Limit Exceeded")
    @required
    title: String

    @const(422)
    @required
    status: Integer

    @const("TRANSFER_LIMIT_EXCEEDED")
    @required
    code: String

    @required
    attributes: TransferLimitExceededAttributes
}
