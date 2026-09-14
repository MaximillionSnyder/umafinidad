<script lang="ts">
  import { goto } from '$app/navigation'
  import { resolve } from '$app/paths'
  import RankingScreen from '$lib/screens/RankingScreen.svelte'
</script>

<RankingScreen onVolver={() => void goto(resolve('/ajustes'))} />
