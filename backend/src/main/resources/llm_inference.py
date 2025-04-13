import argparse
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

def main():
    parser = argparse.ArgumentParser(description='Local LLM Inference')
    parser.add_argument('--model', type=str, required=True, help='Path to the model')
    parser.add_argument('--prompt', type=str, required=True, help='User prompt')
    parser.add_argument('--context', type=str, default='', help='Conversation context')
    parser.add_argument('--temperature', type=float, default=0.7, help='Sampling temperature')
    args = parser.parse_args()

    # Check if Metal is available
    if torch.backends.mps.is_available():
        device = torch.device("mps")
        print("Using Metal GPU acceleration")
    else:
        device = torch.device("cpu")
        print("Metal GPU acceleration not available, falling back to CPU")

    # Load model and tokenizer
    tokenizer = AutoTokenizer.from_pretrained(args.model)
    model = AutoModelForCausalLM.from_pretrained(args.model)
    model = model.to(device)  # Move model to GPU if available

    # Prepare input
    full_prompt = f"{args.context}\nUser: {args.prompt}\nAssistant:"
    inputs = tokenizer(full_prompt, return_tensors="pt")
    inputs = {k: v.to(device) for k, v in inputs.items()}  # Move inputs to GPU if available

    # Generate response
    with torch.no_grad():
        outputs = model.generate(
            inputs.input_ids,
            max_length=2048,
            temperature=args.temperature,
            do_sample=True,
            pad_token_id=tokenizer.eos_token_id
        )

    # Decode and print response
    response = tokenizer.decode(outputs[0], skip_special_tokens=True)
    print(response.split("Assistant:")[-1].strip())

if __name__ == "__main__":
    main() 