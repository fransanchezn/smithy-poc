$version: "2"

namespace com.example

// ------------ TransferLimitExceeded Domain Error ------------
@errorExample([
    {
        title: "Transfer limit exceeded error"
        documentation: "Returned when a transfer exceeds the allowed limit"
        content: {
            type: "https://errors.example.com/domain-error"
            title: "Transfer Limit Exceeded"
            status: 422
            code: "TRANSFER_LIMIT_EXCEEDED"
            attributes: { amount: 15000.00, currency: "USD" }
        }
    }
])
@error("client")
@httpError(422)
structure TransferLimitExceededDomainError with [DomainErrorMixin] {
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
    attributes: TransferLimitAttributes
}

structure TransferLimitAttributes {
    @required
    amount: Double

    @required
    currency: String
}
