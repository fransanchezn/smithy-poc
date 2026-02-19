# Context
Looking at how DomainErrorMixin is implemented for domain error, we now want to support validation errors. These errors also follow the problem detail RFC but they are slightly different.

One of the main differences is that they support more than one error in the schema which looks like this:
```
{
    "type": "/errors/types/validation",
    "title": "Validation Problem",
    "status": 422,
    "detail": "Validation failed",
    "instance": "/api/v1/users",
    "errors": [
        {
            "detail": "Name is required",
            "code": "missing_value",
            "ref": "name",
            "attributes": {
                "attr1":"value1"
            }
        },
        {
            "detail": "Email is required",
            "code": "missing_value",
            "ref": "email",
            "attributes": {
                "attr1":"value1"
            }
        }
    ]
}
```

All validation error have the same top properties type,title,status,detail,instance and errors (array). Then within the errors we will be providing the more specific issues that this validation error is returning

A validation error detail looks like this
```
{
    "detail": "Name is required",
    "code": "missing_value",
    "ref": "name",
    "attributes": {
        "attr1":"value1"
    }
}
```

Where all of them contain the same top level attributes detail,code,ref,attributes

However the attribute 'attributes' is dynamic depending on the code in a very similar way to domain error attributes and error codes.

We want to define all the ValidationError codes individually eventho they can be returnings as one error code with a list of them in our API responses. This is so we can provide in the smithy @operation the list of potential validation codes such as:
```
errors: [
    MissingParameterValidationError
    InvalidValueValidationError
]
```

and the open API generator can generate individual examples for each of these with the proper attribute associated to those codes.


# Task
- Generate a plan to implement these validation errors