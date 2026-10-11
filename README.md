# Task 2
### Code Snippet:
```
@Composable
fun KitchenList(vm: KitchenViewModel) {
var items = mutableListOf<Kitchen>()
LaunchedEffect(items) {
val res = URL(BASE + "?key=" + "sk_live_9f2c41ab")
.readText()
items.addAll(parse(res))
}
LazyColumn { items(items) { KitchenRow(it) } }
}
```

### Problem 1: 	
var items = mutableListOf() is neither remembered nor observable state, so it resets on every recomposition and addAll never triggers a redraw.
#### What breaks: On user side, the list stays blank, which is one-star review.
#### Fix: Hold results in an observable state, a StateFlow in a Viewmodel, that gets collected in UI.

### Problem 2: 
LaunchedEffect(items) is keyed on that list, and a new list exists each recomposition, so the effect restarts and re-fetches every time.

#### What breaks: repeated paid API calls, wasted data. But it becomes part of the billing.
#### Fix: Fetch once from the ViewModel's init, so it survives recomposition and rotation.

### Problem 3:
```
URL(BASE + "?key=" + "sk_live_9f2c41ab")
.readText()
``` 
blocks the main thread, and has no timeout and no error handling.

#### What breaks: On the user end, NetworkOnMainThreadException is thrown, so the app crashes on open. Even if it didn't, the UI would freeze, and any network error would crash it.
#### Fix: Run I/O in withContext(Dispatchers.IO), set timeouts, and catch failures into an Error state.

### Problem 4:
The live secret key 
```
(sk_live_9f2c41ab)
``` 
is hardcoded in source and appended to the URL.

#### What breaks: anyone with the APK or repo can extract it and run up charges.
#### Fix: Remove it from the client-side and call our own backend, which attaches the key server-side.

## Corrected Version:
```
sealed interface KitchenListUiState {
    data object Loading : KitchenListUiState
    data class Success(val kitchens: List<Kitchen>) : KitchenListUiState
    data object Error : KitchenListUiState
}

class KitchenRepository(private val baseUrl: String) {
    suspend fun fetchKitchens(): List<Kitchen> = withContext(Dispatchers.IO) {
        val connection = URL(baseUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            parse(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }
}

class KitchenViewModel(private val repository: KitchenRepository) : ViewModel() {
    private val _state = MutableStateFlow<KitchenListUiState>(KitchenListUiState.Loading)
    val state: StateFlow<KitchenListUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = KitchenListUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                KitchenListUiState.Success(repository.fetchKitchens())
            } catch (e: CancellationException) {
                throw e // never swallow cancellation
            } catch (e: Exception) {
                KitchenListUiState.Error
            }
        }
    }
}

@Composable
fun KitchenList(vm: KitchenViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    when (val s = state) {
        KitchenListUiState.Loading -> CircularProgressIndicator()
        is KitchenListUiState.Success -> LazyColumn {
            items(s.kitchens, key = { it.id }) { KitchenRow(it) }
        }
        KitchenListUiState.Error -> Column {
            Text("Couldn't load kitchens.")
            Button(onClick = vm::load) { Text("Try again") }
        }
    }
}
```

# Task 3
## Release note: Tiffin 1.0 (payments are simulated)

### What I'd test by hand first. 
The signed release build, not the debug one, because shrinking (R8) can break JSON parsing and navigation only in release. On each device I'd walk list -> detail -> Subscribe -> pay -> force-stop and relaunch three times; launches 1 to 3 (paywall on the 3rd only); rotation, "Don't keep activities", and adb shell am kill on every screen; and the charge date after changing time zone and device language (for example Thai, which uses a different calendar year, and Arabic). Devices: an API 24 emulator (our minimum), a 720x1280 small-screen emulator with large fonts, a current-Android phone, and a battery-aggressive OEM phone (vivo or Xiaomi), where the system kills background apps most often.

### Most likely production failure. 
A release-only break in JSON parsing or navigation. Every user would see "Something went wrong" instead of the kitchens. It's handled, so it won't appear as a crash, and today our analytics only writes to Logcat, which nobody can read in production. Before publishing I'd add a second Analytics implementation that sends events to a real backend, then watch kitchen_selected / app_launched per app version. If it collapses toward zero within the first hour of rollout, something is wrong. Play Console's Android vitals would cover crashes and ANRs.

### Rollback at 11 PM. 
Halt the staged rollout in Play Console (I'd start at 5 to 10%, so most users never receive it). Installed copies can't be rolled back, so: check out the last tagged good commit, raise versionCode (Play rejects reused codes), build, sign and upload it as a hotfix. All state is local and the SharedPreferences keys haven't changed, so there's no data migration. There is no remote kill switch today.

### Next three features, in order. 
#### (1) Real Play Billing with purchase verification and restore purchases, since payment is simulated now. 
#### (2) Delivery-slot selection for subscribers, the benefit the paywall implies and I cut. 
#### (3) Kitchen data from a backend with caching and location-based "near you" ranking, which retires the bundled JSON.

# Task 4

## What the Agent got wrong
### S1 veg-menu test
- Tool: Claude (project chat)
- Prompt that led to it (after dicsussing project and planning): 

```
agp = "9.4.1"
kotlin = "2.2.10"
package name = com.example.tiffin
```

- What the AI produced: KitchenParserTest veg check using String.contains("egg")
- What was wrong: substring match, so "veggies" contains "egg" and the test failed on valid data
- How I detected it: the unit test failed and the message named Green Bowl Fit Tiffin / 'Stir-fried veggies'
- Fix: whole-word regex \b(chicken|mutton|fish|eggs?|prawns?|kheema|macher)\b
- Verified by: all 6 tests passing

## Best Prompt
After giving the brief of the project I have to build.
```
Before writing code, help me establish a concrete implementation plan.
Please provide:

1. A concise checklist of all assignment requirements and deliverables.
2. A realistic, time-boxed plan based on the assignment's roughly four-hour estimate, including implementation, debugging, written tasks, and packaging.
3. A minimum viable implementation plan that identifies what must work and what can be cut.
4. The main technical risks, particularly third-launch paywall behavior, purchase persistence, process death, and the 24-hour future charge date.
5. A recommended single-module Android project structure with the smallest sensible set of dependencies.
6. A testing strategy that lets me verify each major requirement as we implement it.

Do not generate the complete codebase yet. First give me the plan, then recommend the first implementation step. I will work in Android Studio and report the actual build results and errors to you.
```