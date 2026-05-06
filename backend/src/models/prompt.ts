export interface PromptOptimizeRequest {
  userDescription?: string;
  /** @deprecated Use activityType instead */
  relationship?: string;
  /** @deprecated Use style (PhotoStyle) instead */
  photoType?: string;
  activityType?: string;
  personTypes?: string[];
  eventContext?: string;
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
