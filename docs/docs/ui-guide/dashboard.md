# Dashboard

The Dashboard is a landing page in Axelix. It summarises every Spring Boot service that Axelix Master currently tracks: the runtime versions in the fleet, how many instances are healthy right now, and the aggregate memory footprint.

The data is collected by Axelix Master from every Spring Boot application that ships an Axelix starter. Until the first instance registers, the page renders empty.

## System's Map

**System's Map** is the row of pie charts across the top of the page. Each chart breaks the fleet down by one software component:

- **Spring Boot**: distribution of Spring Boot versions across all discovered instances.
- **Spring Framework**: distribution of Spring Framework versions.
- **Java**: distribution of major Java versions (for example, `17`, `21`).
- **Kotlin**: distribution of Kotlin versions. Pure-Java instances contribute nothing here, so this chart is empty in fleets without any Kotlin services.

Slice labels show the instance count and the share, formatted as `value (percent%)`. For example, `12 (60%)` means 12 instances run that version and they make up 60% of the chart's component.

### Click a slice to drill into the Wallboard

Clicking a slice navigates to the [Wallboard](./wallboard.md) with a filter already applied for the component and version you clicked. All four charts listed above are clickable.

## Health Status

**Health Status** is a donut chart showing the live status of every discovered instance. Three statuses are tracked:

- **UP** (green): the instance is healthy.
- **DOWN** (red): the instance is not healthy.
- **UNKNOWN** (grey): Axelix Master could not determine the instance's status.

The number in the centre of the donut is labelled **Total Count** and equals the sum of all three statuses, i.e. the total number of instances Axelix is currently tracking.

## Statistics

**Statistics** is a row of three cards next to the Health Status chart:

- **Total count of services**: same total shown in the centre of the Health Status donut.
- **Average Heap Size**: average JVM heap size across all discovered instances.
- **Total Heap Size**: sum of JVM heap size across all discovered instances.

Both heap values are rescaled to a readable unit before display, so the card may read `512 MB`, `2 GB`, and so on depending on the fleet size.

## Related

- [Wallboard](./wallboard.md)
- [Service Profile](./service-profile.md)
- [Details](../features/details)
