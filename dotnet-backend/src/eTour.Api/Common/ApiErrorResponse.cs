namespace eTour.Api.Common;

/// <summary>The single consistent error envelope for every failure response - including validation failures (a deliberate fix of the Java version's inconsistent validation-vs-everything-else shape).</summary>
public record ApiErrorResponse(DateTime Timestamp, int Status, string Message, string Path);
