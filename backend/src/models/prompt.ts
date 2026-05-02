export interface PromptOptimizeRequest {
  userDescription?: string;
  relationship: string;
  photoType: string;
  style: string;
  mood?: string;
  compositionPrefs?: string;
}

export interface ModelParams {
  size?: string;
  quality?: string;
  style?: string;
}

export interface OptimizedPromptResult {
  optimizedPrompt: string;
  negativePrompt: string;
  stylePrompt: string;
  safetyNotes: string[];
  modelParams: ModelParams;
}
