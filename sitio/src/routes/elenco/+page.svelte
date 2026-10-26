<script lang="ts">
  import { goto } from '$app/navigation'
  import { resolve } from '$app/paths'
  import ElencoScreen from '$lib/screens/ElencoScreen.svelte'
</script>

<ElencoScreen onVolver={() => void goto(resolve('/ajustes'))} />
