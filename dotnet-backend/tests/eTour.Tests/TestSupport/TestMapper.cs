using AutoMapper;
using eTour.Application;
using Microsoft.Extensions.Logging.Abstractions;

namespace eTour.Tests.TestSupport;

public static class TestMapper
{
    public static IMapper Create()
    {
        var config = new MapperConfiguration(cfg => cfg.AddMaps(typeof(AssemblyMarker).Assembly), NullLoggerFactory.Instance);
        return config.CreateMapper();
    }
}
