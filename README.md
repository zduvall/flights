<p align="center">
  <img src="assets/flights.png" alt="Flights" height="400">
</p>

<table border='none'>
  <tr>
    <td width="40%" align="center">
    <img src="assets/search-and-status.png" alt="Search and Status" height="300">
    </td>
    <td width="60%">
      <p>A flight-status lookup tool for the Light Phone III, built with the <a href="https://github.com/lightphone/light-sdk">Light SDK</a>.</p>
      <p>Search by flight number to view available flight status and departure and arrival details. Recent searches are saved locally.</p>
    </td>
  </tr>
</table>

<table border='none'>
  <tr>
    <td width="60%">
      <h2>API Key and Privacy</h2>
      <p>Flights uses AeroDataBox through <a href="https://rapidapi.com/aedbx-aedbx/api/aerodatabox">RapidAPI</a>. Add your RapidAPI key in the app's Settings. The key is stored on your device and sent with API requests. Flight-number searches and API-usage checks are sent to RapidAPI to retrieve results. Flights has no app-operated backend, analytics, or advertising.</p>
    </td>
    <td width="40%" align="center">
      <img src="assets/secure.png" alt="Secure" height="200">
    </td>
  </tr>
</table>

## Gallery

<p align="center">
  <img src="assets/search.png" alt="Flight search screen" width="30%" >
  <img src="assets/history.png" alt="Flight history screen" width="30%" >
  <img src="assets/settings.png" alt="Flight settings screen" width="30%" >
</p>

## Development

See the [Light SDK README](https://github.com/lightphone/light-sdk/blob/main/README.md) for SDK setup, build, and run instructions.

For optional Nix development environment and emulator scripts, see [`local-dev/README.md`](local-dev/README.md).
