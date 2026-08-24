using eTour.Api.Common;
using FluentValidation;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using SharpGrip.FluentValidation.AutoValidation.Mvc.Results;
using ValidationResult = FluentValidation.Results.ValidationResult;

namespace eTour.Api.Validation;

/// <summary>
/// Requirement #6: normalizes FluentValidation failures to the same ApiErrorResponse envelope
/// as every other error (see ExceptionHandlingMiddleware), instead of the default
/// ValidationProblemDetails shape - a deliberate fix of the Java version's inconsistency where
/// bean-validation errors came back as a bare field-map unlike everything else.
/// </summary>
public class ValidationResultFactory : IFluentValidationAutoValidationResultFactory
{
    public Task<IActionResult?> CreateActionResult(ActionExecutingContext context,
        ValidationProblemDetails validationProblemDetails, IDictionary<IValidationContext, ValidationResult> validationResults)
    {
        var message = string.Join("; ", validationProblemDetails.Errors
            .SelectMany(field => field.Value.Select(error => $"{field.Key}: {error}")));

        var body = new ApiErrorResponse(DateTime.UtcNow, StatusCodes.Status400BadRequest,
            string.IsNullOrWhiteSpace(message) ? "Validation failed" : message,
            context.HttpContext.Request.Path);

        return Task.FromResult<IActionResult?>(new BadRequestObjectResult(body));
    }
}
