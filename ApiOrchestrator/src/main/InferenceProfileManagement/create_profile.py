import boto3


def create_inference_profile(
    region,
    profile_name,
    model_arn,
    model_name,
    tags=None,
    description=None
):
    bedrock_client= boto3.client('bedrock', region_name=region)

    try:
        params = {
            'inferenceProfileName': profile_name,
            'modelSource': {
                'copyFrom': model_arn
            }
        }

        if description:
            params['description'] = description

        if tags:
            params['tags'] = tags

        response = bedrock_client.create_inference_profile(**params)

        profile_info = {
            'inferenceProfileArn': response['inferenceProfileArn'],
            'inferenceProfileId': response.get('inferenceProfileId'),
            'status': response.get('status'),
            'profileName': profile_name,
            'region': region,
            'model': model_name,
            'modelArn': model_arn
        }

        print(f"Profile's ARN: {profile_info['inferenceProfileArn']}")

        return profile_info
    except bedrock_client.exceptions.BadRequestException as e:
        print(f"ErrorCode: {e.response['Error']['Code']}")
        print(f"Error: {e.response['Error']['Message']}")
        return None


profile = create_inference_profile(
    region="us-east-1",
    profile_name="SubAgentDev",
    model_arn="arn:aws:bedrock:us-east-1:208940303379:inference-profile/us.anthropic.claude-3-5-haiku-20241022-v1:0",
    model_name="Claude 3.5 Haiku",
    tags= [
        {
            'key': 'region',
            'value': 'us-east-1'
        },
        {
            'key': 'project',
            'value': 'ApiSynIQ'
        },
        {
            'key': 'env',
            'value': 'dev'
        },
    ],
    description="Inference profile for my application running on development environment"
)

if profile:
    print(f"Inference Profile Created Successfully!")
    print(f"Name: {profile['profileName']}")
    print(f"ARN: {profile['inferenceProfileArn']}")
    print(f"Region: {profile['region']}")
    print(f"Model: {profile['model']}")

# aws bedrock list-inference-profiles --region us-east-1 --type-equals APPLICATION
